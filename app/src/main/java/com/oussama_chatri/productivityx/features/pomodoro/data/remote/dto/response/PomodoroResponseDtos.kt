package com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import java.time.Instant

data class PomodoroSessionResponseDto(
    val id: String,
    val userId: String?,
    val taskId: String?,
    val type: PomodoroType,
    val plannedDurationSeconds: Int,
    val actualDurationSeconds: Int?,
    val interrupted: Boolean,
    val interruptReason: String?,
    val focusMinutesSetting: Int,
    val shortBreakMinutesSetting: Int,
    val longBreakMinutesSetting: Int,
    val startedAt: String,
    val endedAt: String?,
    val completed: Boolean,
    val actualMinutes: Int?,
    val createdAt: String?,
) {
    fun toDomain(taskTitle: String? = null) = PomodoroSession(
        id                       = id,
        userId                   = userId ?: "",
        taskId                   = taskId,
        taskTitle                = taskTitle,
        type                     = type,
        plannedDurationSeconds   = plannedDurationSeconds,
        actualDurationSeconds    = actualDurationSeconds,
        interrupted              = interrupted,
        interruptReason          = interruptReason,
        focusMinutesSetting      = focusMinutesSetting,
        shortBreakMinutesSetting = shortBreakMinutesSetting,
        longBreakMinutesSetting  = longBreakMinutesSetting,
        startedAt                = Instant.parse(startedAt),
        endedAt                  = endedAt?.let { Instant.parse(it) },
        completed                = completed,
        actualMinutes            = actualMinutes,
        createdAt                = createdAt?.let { Instant.parse(it) } ?: Instant.parse(startedAt),
    )
}

data class PomodoroStatsResponseDto(
    val completedFocusSessionsToday: Long,
    val totalFocusMinutesToday: Long,
    val totalFocusSecondsToday: Long,
) {
    fun toDomain() = PomodoroStats(
        completedFocusSessionsToday = completedFocusSessionsToday,
        totalFocusMinutesToday      = totalFocusMinutesToday,
        totalFocusSecondsToday      = totalFocusSecondsToday,
    )
}

data class PagedSessionsResponseDto(
    val content: List<PomodoroSessionResponseDto>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
)
