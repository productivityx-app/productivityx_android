package com.oussama_chatri.productivityx.features.settings.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePreferencesRequest(
    @SerialName("pomodoroFocusMinutes") val pomodoroFocusMinutes: Int? = null,
    @SerialName("pomodoroShortBreakMinutes") val pomodoroShortBreakMinutes: Int? = null,
    @SerialName("pomodoroLongBreakMinutes") val pomodoroLongBreakMinutes: Int? = null,
    @SerialName("pomodoroCyclesBeforeLongBreak") val pomodoroCyclesBeforeLongBreak: Int? = null,
    @SerialName("pomodoroAutoStartBreaks") val pomodoroAutoStartBreaks: Boolean? = null,
    @SerialName("pomodoroAutoStartFocus") val pomodoroAutoStartFocus: Boolean? = null,
    @SerialName("pomodoroSoundEnabled") val pomodoroSoundEnabled: Boolean? = null,
    @SerialName("notifyTaskReminders") val notifyTaskReminders: Boolean? = null,
    @SerialName("notifyEventReminders") val notifyEventReminders: Boolean? = null,
    @SerialName("notifyPomodoroEnd") val notifyPomodoroEnd: Boolean? = null,
    @SerialName("notifyDailySummary") val notifyDailySummary: Boolean? = null,
    @SerialName("defaultTaskView") val defaultTaskView: String? = null,
    @SerialName("defaultTaskSort") val defaultTaskSort: String? = null,
    @SerialName("showCompletedTasks") val showCompletedTasks: Boolean? = null,
    @SerialName("defaultCalendarView") val defaultCalendarView: String? = null,
    @SerialName("weekStartsOn") val weekStartsOn: String? = null,
    @SerialName("aiContextEnabled") val aiContextEnabled: Boolean? = null,
    @SerialName("aiModel") val aiModel: String? = null,
    @SerialName("compactMode") val compactMode: Boolean? = null,
    @SerialName("appTheme") val appTheme: String? = null
)
