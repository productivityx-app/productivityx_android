package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.EndSessionUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetActiveSessionUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.InterruptSessionUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.StartSessionUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import com.oussama_chatri.productivityx.features.pomodoro.service.PomodoroForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startSessionUseCase:    StartSessionUseCase,
    private val endSessionUseCase:      EndSessionUseCase,
    private val interruptSessionUseCase: InterruptSessionUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val getTodayStatsUseCase:   GetTodayStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    private var timerService: PomodoroForegroundService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val b = binder as? PomodoroForegroundService.PomodoroServiceBinder ?: return
            timerService = b.getService()
            observeTimerState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
        }
    }

    init {
        bindToService()
        loadInitialData()
    }

    fun onEvent(event: PomodoroUiEvent) {
        when (event) {
            is PomodoroUiEvent.SelectType -> {
                if (_uiState.value.isIdle) {
                    _uiState.update { it.copy(selectedType = event.type) }
                }
            }

            PomodoroUiEvent.StartSession   -> startSession()
            PomodoroUiEvent.PauseTimer     -> pauseTimer()
            PomodoroUiEvent.ResumeTimer    -> resumeTimer()
            PomodoroUiEvent.SkipTimer      -> skipTimer()
            PomodoroUiEvent.StopAndInterrupt -> _uiState.update { it.copy(showInterruptDialog = true) }

            is PomodoroUiEvent.ConfirmInterrupt -> confirmInterrupt(event.reason)

            PomodoroUiEvent.DismissInterruptDialog ->
                _uiState.update { it.copy(showInterruptDialog = false, interruptReason = "") }

            is PomodoroUiEvent.LinkTask -> {
                _uiState.update {
                    it.copy(
                        linkedTaskId    = event.taskId,
                        linkedTaskTitle = event.taskTitle,
                        showTaskPickerSheet = false
                    )
                }
            }

            PomodoroUiEvent.UnlinkTask ->
                _uiState.update { it.copy(linkedTaskId = null, linkedTaskTitle = null) }

            PomodoroUiEvent.ShowTaskPicker   -> _uiState.update { it.copy(showTaskPickerSheet = true) }
            PomodoroUiEvent.DismissTaskPicker -> _uiState.update { it.copy(showTaskPickerSheet = false) }

            PomodoroUiEvent.DismissError -> _uiState.update { it.copy(error = null) }

            PomodoroUiEvent.SessionCompleted -> onSessionCompleted()
        }
    }

    private fun startSession() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoadingStart = true, error = null) }

        viewModelScope.launch {
            val result = startSessionUseCase(state.selectedType, state.linkedTaskId)
            when (result) {
                is Resource.Success -> {
                    val session     = result.data
                    val totalSecs   = session.plannedDurationSeconds

                    // Boot the foreground service with the server-assigned session ID
                    val serviceIntent = PomodoroForegroundService.startIntent(
                        context      = context,
                        sessionId    = session.id,
                        type         = session.type,
                        totalSeconds = totalSecs,
                        taskId       = session.taskId,
                        taskTitle    = state.linkedTaskTitle,
                        cycleIndex   = state.cycleIndex
                    )
                    context.startForegroundService(serviceIntent)

                    _uiState.update {
                        it.copy(
                            isLoadingStart  = false,
                            activeSessionId = session.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingStart = false, error = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun pauseTimer() {
        context.startService(PomodoroForegroundService.pauseIntent(context))
    }

    private fun resumeTimer() {
        context.startService(PomodoroForegroundService.resumeIntent(context))
    }

    private fun skipTimer() {
        context.startService(PomodoroForegroundService.skipIntent(context))
    }

    private fun confirmInterrupt(reason: String) {
        val sessionId   = _uiState.value.activeSessionId ?: return
        val elapsedSecs = (timerService?.let {
            val total = _uiState.value.totalSeconds
            total - it.getRemainingSeconds()
        }) ?: 0

        _uiState.update { it.copy(showInterruptDialog = false, isLoadingEnd = true) }
        context.startService(PomodoroForegroundService.stopIntent(context))

        viewModelScope.launch {
            val result = interruptSessionUseCase(sessionId, elapsedSecs.takeIf { it > 0 }, reason.ifBlank { null })
            _uiState.update {
                when (result) {
                    is Resource.Success -> it.copy(
                        isLoadingEnd    = false,
                        activeSessionId = null,
                        timerState      = TimerState.Idle,
                        interruptReason = ""
                    )
                    is Resource.Error   -> it.copy(isLoadingEnd = false, error = result.message)
                    Resource.Loading    -> it
                }
            }
            refreshStats()
        }
    }

    private fun onSessionCompleted() {
        val sessionId = _uiState.value.activeSessionId ?: return
        val totalSecs = _uiState.value.totalSeconds

        _uiState.update { it.copy(isLoadingEnd = true) }

        viewModelScope.launch {
            endSessionUseCase(sessionId, totalSecs)
            val nextCycle     = _uiState.value.cycleIndex + 1
            val longBreakCycle = _uiState.value.cyclesBeforeLongBreak

            val nextType = when {
                _uiState.value.selectedType != PomodoroType.FOCUS -> PomodoroType.FOCUS
                nextCycle % longBreakCycle == 0                   -> PomodoroType.LONG_BREAK
                else                                              -> PomodoroType.SHORT_BREAK
            }

            _uiState.update {
                it.copy(
                    isLoadingEnd    = false,
                    activeSessionId = null,
                    cycleIndex      = nextCycle,
                    selectedType    = nextType,
                    timerState      = TimerState.Idle
                )
            }
            refreshStats()
        }
    }

    private fun observeTimerState() {
        viewModelScope.launch {
            timerService?.timerState?.collect { state ->
                _uiState.update { it.copy(timerState = state) }
                if (state is TimerState.Completed) {
                    onEvent(PomodoroUiEvent.SessionCompleted)
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Restore any active session from the server (resumed from another device)
            when (val result = getActiveSessionUseCase()) {
                is Resource.Success -> {
                    result.data?.let { session ->
                        _uiState.update {
                            it.copy(
                                activeSessionId = session.id,
                                selectedType    = session.type,
                                linkedTaskId    = session.taskId,
                                linkedTaskTitle = session.taskTitle
                            )
                        }
                    }
                }
                else -> Unit
            }
            refreshStats()
        }
    }

    private suspend fun refreshStats() {
        when (val result = getTodayStatsUseCase()) {
            is Resource.Success -> _uiState.update { it.copy(todayStats = result.data) }
            else -> Unit
        }
    }

    private fun bindToService() {
        val intent = Intent(context, PomodoroForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unbindService(serviceConnection) } catch (e: Exception) { /* not bound */ }
    }
}
