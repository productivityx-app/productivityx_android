package com.oussama_chatri.productivityx.features.pomodoro.presentation.event

import com.oussama_chatri.productivityx.core.enums.PomodoroType

sealed class PomodoroUiEvent {

    data class SelectType(val type: PomodoroType) : PomodoroUiEvent()

    data object StartSession : PomodoroUiEvent()

    data object PauseTimer : PomodoroUiEvent()

    data object ResumeTimer : PomodoroUiEvent()

    data object SkipTimer : PomodoroUiEvent()

    data object StopAndInterrupt : PomodoroUiEvent()

    data class ConfirmInterrupt(val reason: String) : PomodoroUiEvent()

    data object DismissInterruptDialog : PomodoroUiEvent()

    data class LinkTask(val taskId: String, val taskTitle: String) : PomodoroUiEvent()

    data object UnlinkTask : PomodoroUiEvent()

    data object ShowTaskPicker : PomodoroUiEvent()

    data object DismissTaskPicker : PomodoroUiEvent()

    data object DismissError : PomodoroUiEvent()

    data object SessionCompleted : PomodoroUiEvent()
}
