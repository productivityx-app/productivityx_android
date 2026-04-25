package com.oussama_chatri.productivityx.features.events.data.repository

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.events.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.events.data.mapper.toEntity
import com.oussama_chatri.productivityx.features.events.data.remote.EventApi
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventRequestDto
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventResponseDto
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.events.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val syncQueueDao: SyncQueueDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val gson: Gson
) : EventRepository {

    override fun observeEvents(from: Instant, to: Instant): Flow<List<Event>> =
        eventDao.observeEvents(cachedUserId(), from.toEpochMilli(), to.toEpochMilli())
            .map { list -> list.map { it.toDomain() } }

    override fun observeUpcomingEvents(limit: Int): Flow<List<Event>> =
        eventDao.observeUpcomingEvents(cachedUserId(), Instant.now().toEpochMilli(), limit)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getEventById(eventId: String): Resource<Event> {
        val userId = cachedUserId()
        val local = eventDao.getEventByIdAndUser(eventId, userId)
        if (local != null) return Resource.Success(local.toDomain())

        val result = safeApiCall { eventApi.getEventById(eventId) }
        return handleEventResponse(result) { dto ->
            eventDao.upsert(dto.toEntity(userId))
        }
    }

    override suspend fun createEvent(
        title: String,
        description: String?,
        location: String?,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean,
        color: String,
        recurrenceRule: String?,
        recurrenceEndAt: Instant?,
        reminderMinutes: Int?
    ): Resource<Event> {
        val userId   = cachedUserId()
        val clientId = UUID.randomUUID().toString()
        val now      = Instant.now().toEpochMilli()

        val entity = EventEntity(
            id                 = clientId,
            userId             = userId,
            recurrenceParentId = null,
            title              = title.trim(),
            description        = description,
            location           = location,
            startAt            = startAt.toEpochMilli(),
            endAt              = endAt.toEpochMilli(),
            isAllDay           = isAllDay,
            color              = color,
            recurrenceRule     = recurrenceRule,
            recurrenceEndAt    = recurrenceEndAt?.toEpochMilli(),
            reminderMinutes    = reminderMinutes,
            isDeleted          = false,
            deletedAt          = null,
            version            = 1,
            syncStatus         = SyncStatus.PENDING,
            createdAt          = now,
            updatedAt          = now,
            pendingOperation   = "CREATE"
        )
        eventDao.upsert(entity)

        val requestDto = buildRequestDto(
            title, description, location, startAt, endAt,
            isAllDay, color, recurrenceRule, recurrenceEndAt, reminderMinutes
        )
        enqueueSync(clientId, SyncOperation.CREATE, gson.toJson(requestDto))

        val remote = safeApiCall { eventApi.createEvent(requestDto) }
        return handleEventResponse(remote) { dto ->
            eventDao.deleteById(clientId)
            eventDao.upsert(dto.toEntity(userId))
            syncQueueDao.deleteByEntity(clientId, EntityType.EVENT)
        }
    }

    override suspend fun updateEvent(
        eventId: String,
        title: String,
        description: String?,
        location: String?,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean,
        color: String,
        recurrenceRule: String?,
        recurrenceEndAt: Instant?,
        reminderMinutes: Int?
    ): Resource<Event> {
        val userId = cachedUserId()
        val now    = Instant.now().toEpochMilli()

        eventDao.getEventById(eventId)?.let { local ->
            eventDao.upsert(
                local.copy(
                    title            = title.trim(),
                    description      = description,
                    location         = location,
                    startAt          = startAt.toEpochMilli(),
                    endAt            = endAt.toEpochMilli(),
                    isAllDay         = isAllDay,
                    color            = color,
                    recurrenceRule   = recurrenceRule,
                    recurrenceEndAt  = recurrenceEndAt?.toEpochMilli(),
                    reminderMinutes  = reminderMinutes,
                    version          = local.version + 1,
                    syncStatus       = SyncStatus.PENDING,
                    updatedAt        = now,
                    pendingOperation = "UPDATE"
                )
            )
        }

        val requestDto = buildRequestDto(
            title, description, location, startAt, endAt,
            isAllDay, color, recurrenceRule, recurrenceEndAt, reminderMinutes
        )
        enqueueSync(eventId, SyncOperation.UPDATE, gson.toJson(requestDto))

        val remote = safeApiCall { eventApi.updateEvent(eventId, requestDto) }
        return handleEventResponse(remote) { dto ->
            eventDao.upsert(dto.toEntity(userId))
            syncQueueDao.deleteByEntity(eventId, EntityType.EVENT)
        }
    }

    override suspend fun deleteEvent(eventId: String): Resource<Unit> {
        val now = Instant.now().toEpochMilli()
        eventDao.getEventById(eventId)?.let {
            eventDao.upsert(
                it.copy(
                    isDeleted        = true,
                    deletedAt        = now,
                    syncStatus       = SyncStatus.PENDING,
                    updatedAt        = now,
                    pendingOperation = "DELETE"
                )
            )
        }
        enqueueSync(eventId, SyncOperation.DELETE, "{}")

        val remote = safeApiCall { eventApi.deleteEvent(eventId) }
        return when (remote) {
            is Resource.Success -> {
                if (remote.data.isSuccessful) {
                    syncQueueDao.deleteByEntity(eventId, EntityType.EVENT)
                    Resource.Success(Unit)
                } else Resource.Error(parseError(remote.data.errorBody()?.string()))
            }
            is Resource.Error   -> remote
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun restoreEvent(eventId: String): Resource<Event> {
        val now = Instant.now().toEpochMilli()
        eventDao.getEventById(eventId)?.let {
            eventDao.upsert(
                it.copy(
                    isDeleted        = false,
                    deletedAt        = null,
                    syncStatus       = SyncStatus.PENDING,
                    updatedAt        = now,
                    pendingOperation = "UPDATE"
                )
            )
        }
        val remote = safeApiCall { eventApi.restoreEvent(eventId) }
        return handleEventResponse(remote) { dto -> eventDao.upsert(dto.toEntity(cachedUserId())) }
    }

    override suspend fun refreshEvents(from: Instant, to: Instant): Resource<Unit> {
        val userId = cachedUserId()
        val result = safeApiCall {
            eventApi.listEvents(
                from = from.toString(),
                to   = to.toString(),
                size = 200
            )
        }
        if (result is Resource.Success && result.data.isSuccessful) {
            val events = result.data.body()?.data?.content ?: emptyList()
            eventDao.upsertAll(events.map { it.toEntity(userId) })
            return Resource.Success(Unit)
        }
        return Resource.Error("Failed to refresh events")
    }

    private fun buildRequestDto(
        title: String,
        description: String?,
        location: String?,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean,
        color: String,
        recurrenceRule: String?,
        recurrenceEndAt: Instant?,
        reminderMinutes: Int?
    ) = EventRequestDto(
        title           = title.trim(),
        description     = description,
        location        = location,
        startAt         = startAt.toString(),
        endAt           = endAt.toString(),
        isAllDay        = isAllDay,
        color           = color,
        recurrenceRule  = recurrenceRule,
        recurrenceEndAt = recurrenceEndAt?.toString(),
        reminderMinutes = reminderMinutes
    )

    private suspend fun enqueueSync(entityId: String, operation: SyncOperation, payload: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id         = UUID.randomUUID().toString(),
                entityType = EntityType.EVENT,
                entityId   = entityId,
                operation  = operation,
                payload    = payload
            )
        )
    }

    private suspend fun handleEventResponse(
        result: Resource<Response<ApiResponse<EventResponseDto>>>,
        onSuccess: suspend (EventResponseDto) -> Unit
    ): Resource<Event> = when (result) {
        is Resource.Success -> {
            val response = result.data
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    onSuccess(dto)
                    Resource.Success(dto.toDomain(cachedUserId()))
                } else Resource.Error("Empty response body")
            } else Resource.Error(parseError(response.errorBody()?.string()))
        }
        is Resource.Error   -> result
        is Resource.Loading -> Resource.Loading
    }

    private fun cachedUserId(): String =
        runCatching { runBlocking { preferencesDataStore.cachedUserId.first() ?: "" } }.getOrDefault("")

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Something went wrong."
        return runCatching {
            Regex("\"message\":\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: "Something went wrong."
        }.getOrDefault("Something went wrong.")
    }
}
