package com.oussama_chatri.productivityx.features.settings.presentation.preferences.event

sealed class PreferencesUiEvent {
    // Search
    data class SettingsSearchQueryChanged(val value: String) : PreferencesUiEvent()
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
    data class HapticFeedbackChanged(val value: Boolean) : PreferencesUiEvent()
    data class QuietHoursStartChanged(val value: Int) : PreferencesUiEvent()
    data class QuietHoursEndChanged(val value: Int) : PreferencesUiEvent()
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
    data class AppThemeChanged(val value: String) : PreferencesUiEvent()
    data class FontScaleChanged(val value: Float) : PreferencesUiEvent()
    data class DensityChanged(val value: String) : PreferencesUiEvent()
    data class LanguageChanged(val value: String) : PreferencesUiEvent()
    // Privacy & Sync
    data class LocalOnlyModeChanged(val value: Boolean) : PreferencesUiEvent()
    data class OfflineModeChanged(val value: Boolean) : PreferencesUiEvent()
    data class AutoSyncChanged(val value: Boolean) : PreferencesUiEvent()
    // Feature flags
    data class FeatureFlagToggled(val key: String, val value: Boolean) : PreferencesUiEvent()
    // Actions
    data object DismissError : PreferencesUiEvent()
    data object DismissSuccess : PreferencesUiEvent()
    data class ExportData(val file: java.io.File) : PreferencesUiEvent()
    data class ImportDataFile(val file: java.io.File) : PreferencesUiEvent()
}
