package com.oussama_chatri.productivityx.features.pomodoro.domain.model

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import java.time.Instant
import java.util.UUID

data class PomodoroSession(
    val id: String,
    val userId: String,
    val taskId: String?,
    val taskTitle: String?,
    val type: PomodoroType,
    val plannedDurationSeconds: Int,
    val actualDurationSeconds: Int?,
    val interrupted: Boolean,
    val interruptReason: String?,
    val focusMinutesSetting: Int,
    val shortBreakMinutesSetting: Int,
    val longBreakMinutesSetting: Int,
    val startedAt: Instant,
    val endedAt: Instant?,
    val completed: Boolean,
    val actualMinutes: Int?,
    val createdAt: Instant
) {
    val isActive: Boolean get() = endedAt == null
    val plannedMinutes: Int get() = plannedDurationSeconds / 60
}
