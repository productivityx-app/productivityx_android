package com.oussama_chatri.productivityx.features.profile.data.repository

import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.profile.data.remote.api.PreferencesApi
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.request.UpdatePreferencesRequest
import com.oussama_chatri.productivityx.features.profile.domain.model.UserPreferencesModel
import com.oussama_chatri.productivityx.features.profile.domain.repository.PreferencesRepository
import com.oussama_chatri.productivityx.features.profile.domain.repository.UpdatePreferencesParams
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val api: PreferencesApi
) : PreferencesRepository {

    override suspend fun getPreferences(): Resource<UserPreferencesModel> = safeApiCall {
        val response = api.getPreferences()
        response.body()?.data?.toDomain()
            ?: error("Empty preferences response")
    }

    override suspend fun updatePreferences(
        request: UpdatePreferencesParams
    ): Resource<UserPreferencesModel> = safeApiCall {
        val response = api.updatePreferences(
            UpdatePreferencesRequest(
                pomodoroFocusMinutes = request.pomodoroFocusMinutes,
                pomodoroShortBreakMinutes = request.pomodoroShortBreakMinutes,
                pomodoroLongBreakMinutes = request.pomodoroLongBreakMinutes,
                pomodoroCyclesBeforeLongBreak = request.pomodoroCyclesBeforeLongBreak,
                pomodoroAutoStartBreaks = request.pomodoroAutoStartBreaks,
                pomodoroAutoStartFocus = request.pomodoroAutoStartFocus,
                pomodoroSoundEnabled = request.pomodoroSoundEnabled,
                notifyTaskReminders = request.notifyTaskReminders,
                notifyEventReminders = request.notifyEventReminders,
                notifyPomodoroEnd = request.notifyPomodoroEnd,
                notifyDailySummary = request.notifyDailySummary,
                defaultTaskView = request.defaultTaskView,
                defaultTaskSort = request.defaultTaskSort,
                showCompletedTasks = request.showCompletedTasks,
                defaultCalendarView = request.defaultCalendarView,
                weekStartsOn = request.weekStartsOn,
                aiContextEnabled = request.aiContextEnabled,
                aiModel = request.aiModel,
                compactMode = request.compactMode
            )
        )
        response.body()?.data?.toDomain()
            ?: error("Empty preferences response")
    }
}
