package com.oussama_chatri.productivityx.features.settings.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.settings.domain.model.UserPreferencesModel

interface PreferencesRepository {
    suspend fun getPreferences(): Resource<UserPreferencesModel>
    suspend fun updatePreferences(request: UpdatePreferencesParams): Resource<UserPreferencesModel>
}

data class UpdatePreferencesParams(
    val pomodoroFocusMinutes: Int? = null,
    val pomodoroShortBreakMinutes: Int? = null,
    val pomodoroLongBreakMinutes: Int? = null,
    val pomodoroCyclesBeforeLongBreak: Int? = null,
    val pomodoroAutoStartBreaks: Boolean? = null,
    val pomodoroAutoStartFocus: Boolean? = null,
    val pomodoroSoundEnabled: Boolean? = null,
    val notifyTaskReminders: Boolean? = null,
    val notifyEventReminders: Boolean? = null,
    val notifyPomodoroEnd: Boolean? = null,
    val notifyDailySummary: Boolean? = null,
    val defaultTaskView: String? = null,
    val defaultTaskSort: String? = null,
    val showCompletedTasks: Boolean? = null,
    val defaultCalendarView: String? = null,
    val weekStartsOn: String? = null,
    val aiContextEnabled: Boolean? = null,
    val aiModel: String? = null,
    val compactMode: Boolean? = null,
    val appTheme: String? = null
)
