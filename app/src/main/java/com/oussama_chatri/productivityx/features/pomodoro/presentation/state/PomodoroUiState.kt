package com.oussama_chatri.productivityx.features.pomodoro.presentation.state

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState

data class PomodoroUiState(
    val timerState: TimerState          = TimerState.Idle,
    val selectedType: PomodoroType      = PomodoroType.FOCUS,
    val cycleIndex: Int                 = 0,
    val cyclesBeforeLongBreak: Int      = 4,
    val focusMinutes: Int               = 25,
    val shortBreakMinutes: Int          = 5,
    val longBreakMinutes: Int           = 15,
    val linkedTaskId: String?           = null,
    val linkedTaskTitle: String?        = null,
    val todayStats: PomodoroStats?      = null,
    val activeSessionId: String?        = null,
    val isLoadingStart: Boolean         = false,
    val isLoadingEnd: Boolean           = false,
    val error: String?                  = null,
    val showTaskPickerSheet: Boolean    = false,
    val showInterruptDialog: Boolean    = false,
    val interruptReason: String         = ""
) {
    val totalSeconds: Int
        get() = when (selectedType) {
            PomodoroType.FOCUS       -> focusMinutes * 60
            PomodoroType.SHORT_BREAK -> shortBreakMinutes * 60
            PomodoroType.LONG_BREAK  -> longBreakMinutes * 60
        }

    val isRunning: Boolean get() = timerState is TimerState.Running
    val isPaused:  Boolean get() = timerState is TimerState.Paused
    val isIdle:    Boolean get() = timerState is TimerState.Idle || timerState is TimerState.Completed
}
