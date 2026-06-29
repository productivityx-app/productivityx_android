package com.oussama_chatri.productivityx.features.settings.presentation.preferences.state

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val settingsSearchQuery: String = "",
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
    val hapticFeedback: Boolean = true,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 8,
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
    val fontScale: Float = 1f,
    val density: String = "COMFORTABLE",
    // Privacy
    val localOnlyMode: Boolean = true,
    // Feature flags
    val offlineMode: Boolean = false,
    val autoSync: Boolean = true,
    val featureFlags: Map<String, Boolean> = mapOf(
        "ai_smart_compose" to false,
        "collaborative_editing" to false,
        "advanced_analytics" to false,
        "voice_notes" to true,
    ),
)
