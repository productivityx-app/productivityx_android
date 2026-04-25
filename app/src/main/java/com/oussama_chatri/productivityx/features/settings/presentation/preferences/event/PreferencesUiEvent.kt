package com.oussama_chatri.productivityx.features.profile.presentation.preferences.event

sealed class PreferencesUiEvent {
    // Pomodoro
    data class FocusMinutesChanged(val value: Int) : PreferencesUiEvent()
    data class ShortBreakMinutesChanged(val value: Int) : PreferencesUiEvent()
    data class LongBreakMinutesChanged(val value: Int) : PreferencesUiEvent()
    data class CyclesChanged(val value: Int) : PreferencesUiEvent()
    data class AutoStartBreaksChanged(val value: Boolean) : PreferencesUiEvent()
    data class AutoStartFocusChanged(val value: Boolean) : PreferencesUiEvent()
    data class SoundEnabledChanged(val value: Boolean) : PreferencesUiEvent()
    // Notifications
    data class NotifyTaskRemindersChanged(val value: Boolean) : PreferencesUiEvent()
    data class NotifyEventRemindersChanged(val value: Boolean) : PreferencesUiEvent()
    data class NotifyPomodoroEndChanged(val value: Boolean) : PreferencesUiEvent()
    data class NotifyDailySummaryChanged(val value: Boolean) : PreferencesUiEvent()
    // Views
    data class DefaultTaskViewChanged(val value: String) : PreferencesUiEvent()
    data class DefaultTaskSortChanged(val value: String) : PreferencesUiEvent()
    data class ShowCompletedTasksChanged(val value: Boolean) : PreferencesUiEvent()
    data class DefaultCalendarViewChanged(val value: String) : PreferencesUiEvent()
    data class WeekStartsOnChanged(val value: String) : PreferencesUiEvent()
    // AI
    data class AiContextEnabledChanged(val value: Boolean) : PreferencesUiEvent()
    data class AiModelChanged(val value: String) : PreferencesUiEvent()
    // Display
    data class CompactModeChanged(val value: Boolean) : PreferencesUiEvent()
    // Actions
    data object DismissError : PreferencesUiEvent()
    data object DismissSuccess : PreferencesUiEvent()
}
