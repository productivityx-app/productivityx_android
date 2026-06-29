package com.oussama_chatri.productivityx.features.settings.data.remote.dto.response

import com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.settings.domain.model.UserPreferencesModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDto(
    @SerialName("id") val id: String,
    @SerialName("userId") val userId: String,
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("timezone") val timezone: String,
    @SerialName("language") val language: String,
    @SerialName("theme") val theme: String,
    @SerialName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain() = ProfileModel(
        id = id,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        avatarUrl = avatarUrl,
        bio = bio,
        timezone = timezone,
        language = language,
        theme = theme,
        updatedAt = updatedAt
    )
}

@Serializable
data class UserPreferencesResponseDto(
    @SerialName("id") val id: String,
    @SerialName("userId") val userId: String,
    @SerialName("pomodoroFocusMinutes") val pomodoroFocusMinutes: Int,
    @SerialName("pomodoroShortBreakMinutes") val pomodoroShortBreakMinutes: Int,
    @SerialName("pomodoroLongBreakMinutes") val pomodoroLongBreakMinutes: Int,
    @SerialName("pomodoroCyclesBeforeLongBreak") val pomodoroCyclesBeforeLongBreak: Int,
    @SerialName("pomodoroAutoStartBreaks") val pomodoroAutoStartBreaks: Boolean,
    @SerialName("pomodoroAutoStartFocus") val pomodoroAutoStartFocus: Boolean,
    @SerialName("pomodoroSoundEnabled") val pomodoroSoundEnabled: Boolean,
    @SerialName("notifyTaskReminders") val notifyTaskReminders: Boolean,
    @SerialName("notifyEventReminders") val notifyEventReminders: Boolean,
    @SerialName("notifyPomodoroEnd") val notifyPomodoroEnd: Boolean,
    @SerialName("notifyDailySummary") val notifyDailySummary: Boolean,
    @SerialName("defaultTaskView") val defaultTaskView: String,
    @SerialName("defaultTaskSort") val defaultTaskSort: String,
    @SerialName("showCompletedTasks") val showCompletedTasks: Boolean,
    @SerialName("defaultCalendarView") val defaultCalendarView: String,
    @SerialName("weekStartsOn") val weekStartsOn: String,
    @SerialName("aiContextEnabled") val aiContextEnabled: Boolean,
    @SerialName("aiModel") val aiModel: String,
    @SerialName("compactMode") val compactMode: Boolean,
    @SerialName("appTheme") val appTheme: String = "DARK",
    @SerialName("language") val language: String = "en",
    @SerialName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain() = UserPreferencesModel(
        id = id,
        userId = userId,
        pomodoroFocusMinutes = pomodoroFocusMinutes,
        pomodoroShortBreakMinutes = pomodoroShortBreakMinutes,
        pomodoroLongBreakMinutes = pomodoroLongBreakMinutes,
        pomodoroCyclesBeforeLongBreak = pomodoroCyclesBeforeLongBreak,
        pomodoroAutoStartBreaks = pomodoroAutoStartBreaks,
        pomodoroAutoStartFocus = pomodoroAutoStartFocus,
        pomodoroSoundEnabled = pomodoroSoundEnabled,
        notifyTaskReminders = notifyTaskReminders,
        notifyEventReminders = notifyEventReminders,
        notifyPomodoroEnd = notifyPomodoroEnd,
        notifyDailySummary = notifyDailySummary,
        defaultTaskView = defaultTaskView,
        defaultTaskSort = defaultTaskSort,
        showCompletedTasks = showCompletedTasks,
        defaultCalendarView = defaultCalendarView,
        weekStartsOn = weekStartsOn,
        aiContextEnabled = aiContextEnabled,
        aiModel = aiModel,
        compactMode = compactMode,
        appTheme = appTheme,
        language = language,
        updatedAt = updatedAt
    )
}
