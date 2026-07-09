package com.oussama_chatri.productivityx.features.pomodoro.data.repository

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.sync.SyncScheduler
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.entity.PomodoroSessionEntity
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.PomodoroApi
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response.PomodoroSessionResponseDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.StartSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepositoryImpl @Inject constructor(
    private val api: PomodoroApi,
    private val pomodoroDao: PomodoroSessionDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncScheduler: SyncScheduler,
    private val preferencesDataStore: PreferencesDataStore,
    private val gson: Gson
) : PomodoroRepository {

    override suspend fun startSession(
        type: PomodoroType,
        taskId: String?
    ): Resource<PomodoroSession> {
        val now = Instant.now()
        val userId = preferencesDataStore.cachedUserId.first() ?: return Resource.Error("Not logged in")
        val sessionId = UUID.randomUUID().toString()

        val entity = PomodoroSessionEntity(
            id = sessionId,
            userId = userId,
            taskId = taskId,
            taskTitle = null,
            type = type,
            plannedDurationSeconds = plannedSecondsForType(type),
            actualDurationSeconds = null,
            completed = false,
            interrupted = false,
            interruptReason = null,
            startedAt = now.toEpochMilli(),
            endedAt = null,
            syncStatus = SyncStatus.PENDING,
            pendingOperation = "CREATE"
        )
        pomodoroDao.insert(entity)

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = sessionId,
                operation = SyncOperation.CREATE,
                payload = gson.toJson(StartSessionRequestDto(type = type, taskId = taskId))
            )
            syncScheduler.scheduleImmediateSync()
        }

        return Resource.Success(entity.toDomain())
    }

    override suspend fun endSession(
        sessionId: String,
        actualDurationSeconds: Int?
    ): Resource<PomodoroSession> {
        val existing = pomodoroDao.getById(sessionId)
            ?: return Resource.Error("Session not found")

        val now = Instant.now()
        val updated = existing.copy(
            actualDurationSeconds = actualDurationSeconds,
            completed = true,
            endedAt = now.toEpochMilli(),
            syncStatus = SyncStatus.PENDING,
            pendingOperation = "UPDATE"
        )
        pomodoroDao.insert(updated)

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = sessionId,
                operation = SyncOperation.UPDATE,
                payload = gson.toJson(
                    mapOf(
                        "action" to "end",
                        "actualDurationSeconds" to (actualDurationSeconds ?: 0)
                    )
                )
            )
            syncScheduler.scheduleImmediateSync()
        }

        return Resource.Success(updated.toDomain())
    }

    override suspend fun interruptSession(
        sessionId: String,
        actualDurationSeconds: Int?,
        reason: String?
    ): Resource<PomodoroSession> {
        val existing = pomodoroDao.getById(sessionId)
            ?: return Resource.Error("Session not found")

        val now = Instant.now()
        val updated = existing.copy(
            actualDurationSeconds = actualDurationSeconds,
            interrupted = true,
            interruptReason = reason,
            endedAt = now.toEpochMilli(),
            syncStatus = SyncStatus.PENDING,
            pendingOperation = "UPDATE"
        )
        pomodoroDao.insert(updated)

        if (isSyncEnabled()) {
            val payload = buildMap<String, Any?> {
                put("action", "interrupt")
                put("actualDurationSeconds", actualDurationSeconds ?: 0)
                if (reason != null) put("interruptReason", reason)
            }
            enqueueSync(
                entityId = sessionId,
                operation = SyncOperation.UPDATE,
                payload = gson.toJson(payload)
            )
            syncScheduler.scheduleImmediateSync()
        }

        return Resource.Success(updated.toDomain())
    }

    override suspend fun getActiveSession(): Resource<PomodoroSession?> {
        val userId = preferencesDataStore.cachedUserId.first()
        if (userId != null) {
            val local = pomodoroDao.getActiveSession(userId)
            if (local != null) return Resource.Success(local.toDomain())
        }

        if (!isSyncEnabled()) return Resource.Success(null)

        return when (val result = safeApiCall { api.getActiveSession() }) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val dto = response.body()?.data
                    if (dto != null) {
                        val entity = dto.toEntity(cachedUserId())
                        pomodoroDao.insert(entity)
                        Resource.Success(dto.toDomain())
                    } else {
                        Resource.Success(null)
                    }
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getSessionById(sessionId: String): Resource<PomodoroSession> {
        val local = pomodoroDao.getById(sessionId)
        if (local != null) return Resource.Success(local.toDomain())

        if (!isSyncEnabled()) return Resource.Error("Session not found locally")

        return when (val result = safeApiCall { api.getSessionById("${ApiConstants.Pomodoro.SESSIONS}/$sessionId") }) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val dto = response.body()?.data ?: return Resource.Error("Empty response from server.")
                    val entity = dto.toEntity(cachedUserId())
                    pomodoroDao.insert(entity)
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getSessions(
        page: Int,
        size: Int,
        taskId: String?
    ): Resource<List<PomodoroSession>> {
        val userId = preferencesDataStore.cachedUserId.first()
        if (userId != null) {
            val local = pomodoroDao.getRecentSessions(userId, size)
            if (local.isNotEmpty()) return Resource.Success(local.map { it.toDomain() })
        }

        if (!isSyncEnabled()) return Resource.Success(emptyList())

        return when (val result = safeApiCall { api.getSessions(page, size, taskId) }) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val sessions = response.body()?.data?.content ?: emptyList()
                    val entities = sessions.map { it.toEntity(cachedUserId()) }
                    pomodoroDao.insertAll(entities)
                    Resource.Success(sessions.map { it.toDomain() })
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getTodayStats(): Resource<PomodoroStats> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        return getDetailedStats(today, today)
    }

    override suspend fun getDetailedStats(startDate: LocalDate, endDate: LocalDate): Resource<PomodoroStats> {
        val userId = preferencesDataStore.cachedUserId.first() ?: return Resource.Error("Not logged in")
        val zoneId = ZoneId.systemDefault()

        val startMs = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMs = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val sessions = pomodoroDao.getFocusSessionsInRange(userId, startMs, endMs)
        val totalFocusSeconds = pomodoroDao.sumFocusSecondsInRange(userId, startMs, endMs)
        val totalSessions = pomodoroDao.countFocusSessionsInRange(userId, startMs, endMs)
        val interruptedSessions = pomodoroDao.countInterruptedFocusSessionsInRange(userId, startMs, endMs)

        // Streak calculation (simplified: check consecutive days with at least one session)
        val allSessions = pomodoroDao.getAllCompletedFocusSessions(userId)
        val focusDays = allSessions.map {
            Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
        }.distinct().sortedDescending()

        var currentStreak = 0
        var today_ = LocalDate.now(zoneId)
        if (focusDays.contains(today_) || focusDays.contains(today_.minusDays(1))) {
            var checkDate = if (focusDays.contains(today_)) today_ else today_.minusDays(1)
            while (focusDays.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        }

        // Longest streak
        var longestStreak = 0
        var tempStreak = 0
        if (focusDays.isNotEmpty()) {
            var lastDate = focusDays.last()
            focusDays.reversed().forEach { date ->
                if (date == lastDate.plusDays(1)) {
                    tempStreak++
                } else {
                    tempStreak = 1
                }
                if (tempStreak > longestStreak) longestStreak = tempStreak
                lastDate = date
            }
        }

        val qualityScore = if (totalSessions > 0) {
            1f - (interruptedSessions.toFloat() / totalSessions.toFloat())
        } else 1f

        val heatMap = sessions.groupBy {
            Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
        }.mapValues { (_, daySessions) ->
            daySessions.sumOf { it.actualDurationSeconds ?: 0 } / 60
        }

        val categoryDist = sessions.groupBy { it.taskTitle ?: "Uncategorized" }
            .mapValues { (_, catSessions) ->
                catSessions.sumOf { it.actualDurationSeconds ?: 0 } / 60
            }

        val dailyGoal = preferencesDataStore.pomodoroDailyGoal.first()
        val weeklyGoal = preferencesDataStore.pomodoroWeeklyGoal.first()

        return Resource.Success(
            PomodoroStats(
                completedFocusSessionsToday = if (startDate == LocalDate.now(zoneId)) totalSessions else 0, // Simplified
                totalFocusMinutesToday = if (startDate == LocalDate.now(zoneId)) totalFocusSeconds / 60 else 0,
                totalFocusSecondsToday = if (startDate == LocalDate.now(zoneId)) totalFocusSeconds else 0,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                focusQualityScore = qualityScore,
                weeklyHeatMap = heatMap,
                categoryDistribution = categoryDist,
                dailyGoalMinutes = dailyGoal,
                weeklyGoalMinutes = weeklyGoal,
                totalFocusTimeAllTime = allSessions.sumOf { (it.actualDurationSeconds ?: 0).toLong() }
            )
        )
    }

    override suspend fun updateGoals(dailyMinutes: Int, weeklyMinutes: Int): Resource<Unit> {
        preferencesDataStore.setPomodoroGoals(dailyMinutes, weeklyMinutes)
        return Resource.Success(Unit)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeSessions(): Flow<List<PomodoroSession>> =
        preferencesDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            pomodoroDao.observeSessions(userId).map { entities ->
                entities.map { it.toDomain() }
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
        interruptReason = interruptReason,
        startedAt = runCatching { java.time.Instant.parse(startedAt).toEpochMilli() }.getOrDefault(System.currentTimeMillis()),
        endedAt = endedAt?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() },
        syncStatus = SyncStatus.SYNCED
    )

    private suspend fun enqueueSync(entityId: String, operation: SyncOperation, payload: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = EntityType.POMODORO,
                entityId = entityId,
                operation = operation,
                payload = payload
            )
        )
    }

    private suspend fun cachedUserId(): String =
        preferencesDataStore.cachedUserId.first() ?: ""

    private suspend fun isSyncEnabled(): Boolean = preferencesDataStore.isSyncEnabled()

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Something went wrong."
        return try {
            Regex("\"message\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
                ?: "Something went wrong."
        } catch (e: Exception) {
            "Something went wrong."
        }
    }

    private fun plannedSecondsForType(type: PomodoroType): Int = when (type) {
        PomodoroType.FOCUS -> 25 * 60
        PomodoroType.SHORT_BREAK -> 5 * 60
        PomodoroType.LONG_BREAK -> 15 * 60
    }
}
