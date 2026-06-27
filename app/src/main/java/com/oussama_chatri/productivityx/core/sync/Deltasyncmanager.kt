package com.oussama_chatri.productivityx.core.sync

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
        if (delta.notes.isNotEmpty()) {
            val toUpsert = mutableListOf<NoteEntity>()
            val tagRefs = mutableListOf<Pair<String, List<String>>>()
            for (dto in delta.notes) {
                if (dto.deleted) {
                    noteDao.deleteById(dto.id)
                } else {
                    val (entity, refs) = dto.toEntityWithRefs(userId)
                    toUpsert.add(entity)
                    tagRefs.add(dto.id to refs.map { it.tagId })
                }
            }
            if (toUpsert.isNotEmpty()) {
                noteDao.upsertAll(toUpsert)
                for ((noteId, tagIds) in tagRefs) {
                    runCatching { noteDao.replaceNoteTags(noteId, tagIds) }
                }
            }
        }

        // Tasks
        if (delta.tasks.isNotEmpty()) {
            val toUpsert = mutableListOf<com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity>()
            for (dto in delta.tasks) {
                if (dto.deleted) {
                    taskDao.deleteById(dto.id)
                } else {
                    toUpsert.add(dto.toEntity(userId))
                }
            }
            if (toUpsert.isNotEmpty()) {
                taskDao.insertAll(toUpsert)
            }
        }

        // Events
        if (delta.events.isNotEmpty()) {
            val toUpsert = mutableListOf<EventEntity>()
            for (dto in delta.events) {
                if (dto.deleted) {
                    eventDao.deleteById(dto.id)
                } else {
                    toUpsert.add(dto.toEntity(userId))
                }
            }
            if (toUpsert.isNotEmpty()) {
                eventDao.upsertAll(toUpsert)
            }
        }

        // Pomodoro sessions
        if (delta.pomodoroSessions.isNotEmpty()) {
            val toUpsert = mutableListOf<PomodoroSessionEntity>()
            for (dto in delta.pomodoroSessions) {
                toUpsert.add(dto.toEntity(userId))
            }
            if (toUpsert.isNotEmpty()) {
                pomodoroDao.insertAll(toUpsert)
            }
        }
    }
}

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
    startedAt = runCatching { Instant.parse(startedAt).toEpochMilli() }.getOrDefault(System.currentTimeMillis()),
    endedAt = endedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() },
)
