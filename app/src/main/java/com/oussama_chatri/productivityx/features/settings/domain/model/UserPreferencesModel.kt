package com.oussama_chatri.productivityx.features.profile.domain.model

data class UserPreferencesModel(
    val id: String,
    val userId: String,
    // Pomodoro
    val pomodoroFocusMinutes: Int,
    val pomodoroShortBreakMinutes: Int,
    val pomodoroLongBreakMinutes: Int,
    val pomodoroCyclesBeforeLongBreak: Int,
    val pomodoroAutoStartBreaks: Boolean,
    val pomodoroAutoStartFocus: Boolean,
    val pomodoroSoundEnabled: Boolean,
    // Notifications
    val notifyTaskReminders: Boolean,
    val notifyEventReminders: Boolean,
    val notifyPomodoroEnd: Boolean,
    val notifyDailySummary: Boolean,
    // Views
    val defaultTaskView: String,
    val defaultTaskSort: String,
    val showCompletedTasks: Boolean,
    val defaultCalendarView: String,
    val weekStartsOn: String,
    // AI
    val aiContextEnabled: Boolean,
    val aiModel: String,
    // Display
    val compactMode: Boolean,
    val updatedAt: String?
)
