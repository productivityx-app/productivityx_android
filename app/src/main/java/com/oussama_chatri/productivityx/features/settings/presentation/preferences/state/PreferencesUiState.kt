package com.oussama_chatri.productivityx.features.settings.presentation.preferences.state

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Pomodoro
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val pomodoroCyclesBeforeLongBreak: Int = 4,
    val pomodoroAutoStartBreaks: Boolean = false,
    val pomodoroAutoStartFocus: Boolean = false,
    val pomodoroSoundEnabled: Boolean = true,
    // Notifications
    val notifyTaskReminders: Boolean = true,
    val notifyEventReminders: Boolean = true,
    val notifyPomodoroEnd: Boolean = true,
    val notifyDailySummary: Boolean = false,
    // Views
    val defaultTaskView: String = "LIST",
    val defaultTaskSort: String = "DUE_DATE",
    val showCompletedTasks: Boolean = false,
    val defaultCalendarView: String = "WEEK",
    val weekStartsOn: String = "MON",
    // AI
    val aiContextEnabled: Boolean = true,
    val aiModel: String = "gemini-2.0-flash",
    // Display
    val compactMode: Boolean = false,
    val appTheme: String = "DARK",
    // Privacy
    val localOnlyMode: Boolean = true
)
