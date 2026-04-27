package com.oussama_chatri.productivityx.features.pomodoro.domain.model

import com.oussama_chatri.productivityx.core.enums.PomodoroType

sealed class TimerState {

    data object Idle : TimerState()

    data class Running(
        val sessionId: String,
        val type: PomodoroType,
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val taskId: String?,
        val taskTitle: String?,
        val cycleIndex: Int
    ) : TimerState() {
        val progressFraction: Float
            get() = if (totalSeconds == 0) 0f
                    else 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
    }

    data class Paused(
        val sessionId: String,
        val type: PomodoroType,
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val taskId: String?,
        val taskTitle: String?,
        val cycleIndex: Int
    ) : TimerState() {
        val progressFraction: Float
            get() = if (totalSeconds == 0) 0f
                    else 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
    }

    data class Completed(
        val type: PomodoroType,
        val cycleIndex: Int
    ) : TimerState()
}
