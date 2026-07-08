package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.data.DataExportImportManager
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.settings.domain.repository.UpdatePreferencesParams
import com.oussama_chatri.productivityx.features.settings.domain.usecase.GetPreferencesUseCase
import com.oussama_chatri.productivityx.features.settings.domain.usecase.UpdatePreferencesUseCase
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.event.PreferencesUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.state.PreferencesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val prefs: PreferencesDataStore,
    private val exportImportManager: DataExportImportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState(isLoading = true))
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    init {
        load()
    }

    fun onEvent(event: PreferencesUiEvent) {
        when (event) {
            is PreferencesUiEvent.SettingsSearchQueryChanged ->
                _uiState.update { it.copy(settingsSearchQuery = event.value) }
            is PreferencesUiEvent.FocusMinutesChanged ->
                mutate { it.copy(pomodoroFocusMinutes = event.value.coerceIn(1, 120)) }
            is PreferencesUiEvent.ShortBreakMinutesChanged ->
                mutate { it.copy(pomodoroShortBreakMinutes = event.value.coerceIn(1, 60)) }
            is PreferencesUiEvent.LongBreakMinutesChanged ->
                mutate { it.copy(pomodoroLongBreakMinutes = event.value.coerceIn(1, 60)) }
            is PreferencesUiEvent.CyclesChanged ->
                mutate { it.copy(pomodoroCyclesBeforeLongBreak = event.value.coerceIn(1, 10)) }
            is PreferencesUiEvent.AutoStartBreaksChanged ->
                mutate { it.copy(pomodoroAutoStartBreaks = event.value) }
            is PreferencesUiEvent.AutoStartFocusChanged ->
                mutate { it.copy(pomodoroAutoStartFocus = event.value) }
            is PreferencesUiEvent.SoundEnabledChanged ->
                mutate { it.copy(pomodoroSoundEnabled = event.value) }
            is PreferencesUiEvent.NotifyTaskRemindersChanged ->
                mutate { it.copy(notifyTaskReminders = event.value) }
            is PreferencesUiEvent.NotifyEventRemindersChanged ->
                mutate { it.copy(notifyEventReminders = event.value) }
            is PreferencesUiEvent.NotifyPomodoroEndChanged ->
                mutate { it.copy(notifyPomodoroEnd = event.value) }
            is PreferencesUiEvent.NotifyDailySummaryChanged ->
                mutate { it.copy(notifyDailySummary = event.value) }
            is PreferencesUiEvent.HapticFeedbackChanged ->
                mutate { it.copy(hapticFeedback = event.value) }
            is PreferencesUiEvent.QuietHoursStartChanged ->
                mutate { it.copy(quietHoursStart = event.value.coerceIn(0, 23)) }
            is PreferencesUiEvent.QuietHoursEndChanged ->
                mutate { it.copy(quietHoursEnd = event.value.coerceIn(0, 23)) }
            is PreferencesUiEvent.DefaultTaskViewChanged ->
                mutate { it.copy(defaultTaskView = event.value) }
            is PreferencesUiEvent.DefaultTaskSortChanged ->
                mutate { it.copy(defaultTaskSort = event.value) }
            is PreferencesUiEvent.ShowCompletedTasksChanged ->
                mutate { it.copy(showCompletedTasks = event.value) }
            is PreferencesUiEvent.DefaultCalendarViewChanged ->
                mutate { it.copy(defaultCalendarView = event.value) }
            is PreferencesUiEvent.WeekStartsOnChanged ->
                mutate { it.copy(weekStartsOn = event.value) }
            is PreferencesUiEvent.AiContextEnabledChanged ->
                mutate { it.copy(aiContextEnabled = event.value) }
            is PreferencesUiEvent.AiModelChanged ->
                mutate { it.copy(aiModel = event.value) }
            is PreferencesUiEvent.CompactModeChanged ->
                mutate { it.copy(compactMode = event.value) }
            is PreferencesUiEvent.AppThemeChanged -> {
                _uiState.update { it.copy(appTheme = event.value) }
                viewModelScope.launch { prefs.setTheme(event.value) }
                scheduleSave()
            }
            is PreferencesUiEvent.FontScaleChanged ->
                mutate { it.copy(fontScale = event.value.coerceIn(0.7f, 1.5f)) }
            is PreferencesUiEvent.DensityChanged ->
                mutate { it.copy(density = event.value) }
            is PreferencesUiEvent.LanguageChanged -> {
                _uiState.update { it.copy(language = event.value) }
                viewModelScope.launch { prefs.setLanguage(event.value) }
                scheduleSave()
            }
            is PreferencesUiEvent.LocalOnlyModeChanged -> {
                _uiState.update { it.copy(localOnlyMode = event.value) }
                viewModelScope.launch { prefs.setLocalOnlyMode(event.value) }
            }
            is PreferencesUiEvent.OfflineModeChanged ->
                mutate { it.copy(offlineMode = event.value) }
            is PreferencesUiEvent.AutoSyncChanged ->
                mutate { it.copy(autoSync = event.value) }
            is PreferencesUiEvent.FeatureFlagToggled ->
                mutate { it.copy(featureFlags = it.featureFlags + (event.key to event.value)) }
            PreferencesUiEvent.DismissError ->
                _uiState.update { it.copy(errorMessage = null) }
            PreferencesUiEvent.DismissSuccess ->
                _uiState.update { it.copy(successMessage = null) }
            is PreferencesUiEvent.ExportData -> {
                viewModelScope.launch {
                    try {
                        exportImportManager.exportToFile(event.file)
                        _uiState.update { it.copy(successMessage = "Data exported successfully") }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Export failed: ${e.message}") }
                    }
                }
            }
            is PreferencesUiEvent.ImportDataFile -> {
                viewModelScope.launch {
                    try {
                        exportImportManager.importFromFile(event.file)
                        _uiState.update { it.copy(successMessage = "Data imported successfully") }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Import failed: ${e.message}") }
                    }
                }
            }
        }
    }

    private fun mutate(transform: (PreferencesUiState) -> PreferencesUiState) {
        _uiState.update(transform)
        scheduleSave()
    }

    private fun scheduleSave() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(600)
            save()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val localOnly = prefs.localOnlyMode.first()
            when (val result = getPreferencesUseCase()) {
                is Resource.Success -> {
                    val d = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            localOnlyMode = localOnly,
                            pomodoroFocusMinutes = d.pomodoroFocusMinutes,
                            pomodoroShortBreakMinutes = d.pomodoroShortBreakMinutes,
                            pomodoroLongBreakMinutes = d.pomodoroLongBreakMinutes,
                            pomodoroCyclesBeforeLongBreak = d.pomodoroCyclesBeforeLongBreak,
                            pomodoroAutoStartBreaks = d.pomodoroAutoStartBreaks,
                            pomodoroAutoStartFocus = d.pomodoroAutoStartFocus,
                            pomodoroSoundEnabled = d.pomodoroSoundEnabled,
                            notifyTaskReminders = d.notifyTaskReminders,
                            notifyEventReminders = d.notifyEventReminders,
                            notifyPomodoroEnd = d.notifyPomodoroEnd,
                            notifyDailySummary = d.notifyDailySummary,
                            defaultTaskView = d.defaultTaskView,
                            defaultTaskSort = d.defaultTaskSort,
                            showCompletedTasks = d.showCompletedTasks,
                            defaultCalendarView = d.defaultCalendarView,
                            weekStartsOn = d.weekStartsOn,
                            aiContextEnabled = d.aiContextEnabled,
                            aiModel = d.aiModel,
                            compactMode = d.compactMode,
                            appTheme = d.appTheme
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun save() {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        val result = updatePreferencesUseCase(
            UpdatePreferencesParams(
                pomodoroFocusMinutes = s.pomodoroFocusMinutes,
                pomodoroShortBreakMinutes = s.pomodoroShortBreakMinutes,
                pomodoroLongBreakMinutes = s.pomodoroLongBreakMinutes,
                pomodoroCyclesBeforeLongBreak = s.pomodoroCyclesBeforeLongBreak,
                pomodoroAutoStartBreaks = s.pomodoroAutoStartBreaks,
                pomodoroAutoStartFocus = s.pomodoroAutoStartFocus,
                pomodoroSoundEnabled = s.pomodoroSoundEnabled,
                notifyTaskReminders = s.notifyTaskReminders,
                notifyEventReminders = s.notifyEventReminders,
                notifyPomodoroEnd = s.notifyPomodoroEnd,
                notifyDailySummary = s.notifyDailySummary,
                defaultTaskView = s.defaultTaskView,
                defaultTaskSort = s.defaultTaskSort,
                showCompletedTasks = s.showCompletedTasks,
                defaultCalendarView = s.defaultCalendarView,
                weekStartsOn = s.weekStartsOn,
                aiContextEnabled = s.aiContextEnabled,
                aiModel = s.aiModel,
                compactMode = s.compactMode,
                appTheme = s.appTheme,
                language = s.language
            )
        )

        _uiState.update {
            when (result) {
                is Resource.Success -> it.copy(isSaving = false, successMessage = "Saved")
                is Resource.Error -> it.copy(isSaving = false, errorMessage = result.message)
                Resource.Loading -> it.copy(isSaving = false)
            }
        }
    }
}
