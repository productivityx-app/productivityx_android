package com.oussama_chatri.productivityx.core.sync

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.events.data.mapper.toEntity
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventResponseDto
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntityWithRefs
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.entity.PomodoroSessionEntity
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response.PomodoroSessionResponseDto
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.TaskResponseDto
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private data class DeltaSyncEnvelope(
    val success: Boolean = false,
    val data: DeltaSyncData? = null,
)

private data class DeltaSyncData(
    val notes: List<NoteResponseDto> = emptyList(),
    val tasks: List<TaskResponseDto> = emptyList(),
    val events: List<EventResponseDto> = emptyList(),
    @SerializedName("pomodoroSessions")
    val pomodoroSessions: List<PomodoroSessionResponseDto> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val syncedAt: String? = null,
    val totalChanges: Int = 0
)

@Singleton
class DeltaSyncManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenStorage: TokenStorage,
    private val preferencesDataStore: PreferencesDataStore,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val eventDao: EventDao,
    private val pomodoroDao: PomodoroSessionDao,
    private val syncQueueDao: SyncQueueDao,
    private val conflictResolver: ConflictResolver,
    private val gson: Gson,
) {
    suspend fun pullDelta() {
        if (!preferencesDataStore.isSyncEnabled()) return
        val token = tokenStorage.getAccessToken() ?: return
        val userId = preferencesDataStore.cachedUserId.first() ?: return

        var lastSyncedAt = preferencesDataStore.lastSyncedAt.first()
        var cursor: String? = null

        do {
            val since = Instant.ofEpochMilli(lastSyncedAt).toString()
            val urlBuilder = StringBuilder()
                .append(ApiConstants.BASE_URL)
                .append(ApiConstants.Sync.DELTA)
                .append("?since=").append(since)
            if (cursor != null) {
                urlBuilder.append("&cursor=").append(cursor)
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header(ApiConstants.HEADER_AUTHORIZATION, "${ApiConstants.HEADER_BEARER_PREFIX}$token")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return

            val body = response.body?.string() ?: return
            val envelope = gson.fromJson(body, DeltaSyncEnvelope::class.java)
            val delta = envelope.data ?: return

            applyDelta(delta, userId)

            cursor = delta.nextCursor
            if (!delta.hasMore && delta.syncedAt != null) {
                val serverTime = runCatching { Instant.parse(delta.syncedAt).toEpochMilli() }.getOrNull()
                if (serverTime != null) {
                    preferencesDataStore.setLastSyncedAt(serverTime)
                }
            }
        } while (cursor != null)
    }

    private suspend fun applyDelta(delta: DeltaSyncData, userId: String) {
        // Notes
        for (dto in delta.notes) {
            if (syncQueueDao.pendingCountByEntity(dto.id, EntityType.NOTE) > 0) continue
            if (dto.deleted) {
                noteDao.deleteById(dto.id)
            } else {
                val local = noteDao.getNoteById(dto.id)
                val remoteUpdatedAt = parseInstantMs(dto.updatedAt)
                if (local == null || conflictResolver.remoteWins(local.note.updatedAt, remoteUpdatedAt)) {
                    val (entity, refs) = dto.toEntityWithRefs(userId)
                    noteDao.upsert(entity)
                    runCatching { noteDao.replaceNoteTags(entity.id, refs.map { it.tagId }) }
                }
            }
        }

        // Tasks
        for (dto in delta.tasks) {
            if (syncQueueDao.pendingCountByEntity(dto.id, EntityType.TASK) > 0) continue
            if (dto.deleted) {
                taskDao.deleteById(dto.id)
            } else {
                val local = taskDao.getById(dto.id)
                val remoteUpdatedAt = parseInstantMs(dto.updatedAt)
                if (local == null || conflictResolver.remoteWins(local.updatedAt.toEpochMilli(), remoteUpdatedAt)) {
                    taskDao.insert(dto.toEntity(userId))
                }
            }
        }

        // Events
        for (dto in delta.events) {
            if (syncQueueDao.pendingCountByEntity(dto.id, EntityType.EVENT) > 0) continue
            if (dto.deleted) {
                eventDao.deleteById(dto.id)
            } else {
                val local = eventDao.getEventById(dto.id)
                val remoteUpdatedAt = parseInstantMs(dto.updatedAt)
                if (local == null || conflictResolver.remoteWins(local.updatedAt, remoteUpdatedAt)) {
                    eventDao.upsert(dto.toEntity(userId))
                }
            }
        }

        // Pomodoro sessions — no updatedAt field, so skip timestamp check but still respect pending outbox
        for (dto in delta.pomodoroSessions) {
            if (syncQueueDao.pendingCountByEntity(dto.id, EntityType.POMODORO) > 0) continue
            pomodoroDao.insert(dto.toEntity(userId))
        }
    }
}

private fun parseInstantMs(value: String?): Long =
    if (value.isNullOrBlank()) System.currentTimeMillis()
    else runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(System.currentTimeMillis())

private fun PomodoroSessionResponseDto.toEntity(userId: String) = PomodoroSessionEntity(
    id = id,
    userId = this.userId ?: userId,
    taskId = taskId,
    taskTitle = null,
    type = type,
    plannedDurationSeconds = plannedDurationSeconds,
    actualDurationSeconds = actualDurationSeconds,
    completed = completed,
    interrupted = interrupted,
    interruptReason = interruptReason,
    startedAt = parseInstantMs(startedAt),
    endedAt = endedAt?.let { parseInstantMs(it) },
    syncStatus = SyncStatus.SYNCED,
)
