package com.oussama_chatri.productivityx.features.pomodoro.presentation.event

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.AmbientSound

sealed class PomodoroUiEvent {
    data class SelectType(val type: PomodoroType) : PomodoroUiEvent()
    data object StartSession : PomodoroUiEvent()
    data object PauseTimer : PomodoroUiEvent()
    data object ResumeTimer : PomodoroUiEvent()
    data object SkipTimer : PomodoroUiEvent()
    data object StopAndInterrupt : PomodoroUiEvent()
    data class ConfirmInterrupt(val reason: String) : PomodoroUiEvent()
    data object DismissInterruptDialog : PomodoroUiEvent()
    
    data object ShowTaskPicker : PomodoroUiEvent()
    data class SelectTask(val taskId: String, val title: String) : PomodoroUiEvent()
    data object UnlinkTask : PomodoroUiEvent()
    
    // New Modernization Events
    data object ToggleFocusMode : PomodoroUiEvent()
    data class SelectAmbientSound(val sound: AmbientSound) : PomodoroUiEvent()
    data object ToggleDnd : PomodoroUiEvent()
    data object Extend1Min : PomodoroUiEvent()
}
