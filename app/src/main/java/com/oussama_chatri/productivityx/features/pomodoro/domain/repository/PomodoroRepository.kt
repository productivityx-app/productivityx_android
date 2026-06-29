package com.oussama_chatri.productivityx.features.pomodoro.domain.repository

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PomodoroRepository {

    suspend fun startSession(type: PomodoroType, taskId: String?): Resource<PomodoroSession>

    suspend fun endSession(sessionId: String, actualDurationSeconds: Int?): Resource<PomodoroSession>

    suspend fun interruptSession(
        sessionId: String,
        actualDurationSeconds: Int?,
        reason: String?
    ): Resource<PomodoroSession>

    suspend fun getActiveSession(): Resource<PomodoroSession?>

    suspend fun getSessionById(sessionId: String): Resource<PomodoroSession>

    suspend fun getSessions(page: Int, size: Int, taskId: String?): Resource<List<PomodoroSession>>

    suspend fun getTodayStats(): Resource<PomodoroStats>

    suspend fun getDetailedStats(startDate: LocalDate, endDate: LocalDate): Resource<PomodoroStats>

    suspend fun updateGoals(dailyMinutes: Int, weeklyMinutes: Int): Resource<Unit>

    fun observeSessions(): Flow<List<PomodoroSession>>
}
