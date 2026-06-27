package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val getTodayStatsUseCase: GetTodayStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    private var tickJob: Job? = null

    init { refreshStats() }

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
        if (!state.isIdle) return

        val sessionId = UUID.randomUUID().toString()
        val totalSecs = state.totalSeconds

        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                isLoadingStart  = false,
                timerState      = TimerState.Running(
                    sessionId        = sessionId,
                    type             = state.selectedType,
                    totalSeconds     = totalSecs,
                    remainingSeconds = totalSecs,
                    taskId           = state.linkedTaskId,
                    taskTitle        = state.linkedTaskTitle,
                    cycleIndex       = state.cycleIndex
                )
            )
        }

        startTicking(sessionId, state.selectedType, totalSecs, state.linkedTaskId, state.linkedTaskTitle, state.cycleIndex)
    }

    private fun startTicking(
        sessionId: String,
        type: PomodoroType,
        totalSeconds: Int,
        taskId: String?,
        taskTitle: String?,
        cycleIndex: Int
    ) {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                val current = _uiState.value.timerState
                if (current is TimerState.Paused) {
                    while (_uiState.value.timerState is TimerState.Paused) delay(200L)
                    continue
                }
                if (current !is TimerState.Running) break
                remaining--
                _uiState.update {
                    it.copy(
                        timerState = current.copy(remainingSeconds = remaining)
                    )
                }
            }
            val finalState = _uiState.value.timerState
            if (finalState is TimerState.Running) {
                _uiState.update {
                    it.copy(
                        timerState = TimerState.Completed(finalState.type, finalState.cycleIndex)
                    )
                }
                onEvent(PomodoroUiEvent.SessionCompleted)
            }
        }
    }

    private fun pauseTimer() {
        val current = _uiState.value.timerState as? TimerState.Running ?: return
        _uiState.update {
            it.copy(
                timerState = TimerState.Paused(
                    sessionId        = current.sessionId,
                    type             = current.type,
                    totalSeconds     = current.totalSeconds,
                    remainingSeconds = current.remainingSeconds,
                    taskId           = current.taskId,
                    taskTitle        = current.taskTitle,
                    cycleIndex       = current.cycleIndex
                )
            )
        }
    }

    private fun resumeTimer() {
        val current = _uiState.value.timerState as? TimerState.Paused ?: return
        _uiState.update {
            it.copy(
                timerState = TimerState.Running(
                    sessionId        = current.sessionId,
                    type             = current.type,
                    totalSeconds     = current.totalSeconds,
                    remainingSeconds = current.remainingSeconds,
                    taskId           = current.taskId,
                    taskTitle        = current.taskTitle,
                    cycleIndex       = current.cycleIndex
                )
            )
        }
    }

    private fun skipTimer() {
        val current = _uiState.value.timerState
        val (type, cycle) = when (current) {
            is TimerState.Running -> current.type to current.cycleIndex
            is TimerState.Paused  -> current.type to current.cycleIndex
            else                  -> return
        }
        tickJob?.cancel()
        _uiState.update {
            it.copy(
                timerState = TimerState.Completed(type, cycle),
                cycleIndex = cycle + 1,
                selectedType = PomodoroType.FOCUS
            )
        }
        _snackbar.tryEmit("Session skipped")
        refreshStats()
    }

    private fun confirmInterrupt(reason: String) {
        val state = _uiState.value
        val current = state.timerState
        val elapsedSecs = when (current) {
            is TimerState.Running -> current.totalSeconds - current.remainingSeconds
            is TimerState.Paused  -> current.totalSeconds - current.remainingSeconds
            else                  -> 0
        }
        tickJob?.cancel()
        _uiState.update {
            it.copy(
                showInterruptDialog = false,
                timerState          = TimerState.Idle,
                activeSessionId     = null,
                interruptReason     = ""
            )
        }
        _snackbar.tryEmit("Session interrupted · ${elapsedSecs / 60}m focus credited")
        refreshStats()
    }

    private fun onSessionCompleted() {
        tickJob?.cancel()
        val state = _uiState.value
        val nextCycle     = state.cycleIndex + 1
        val longBreakCycle = state.cyclesBeforeLongBreak

        val nextType = when {
            state.selectedType != PomodoroType.FOCUS -> PomodoroType.FOCUS
            nextCycle % longBreakCycle == 0          -> PomodoroType.LONG_BREAK
            else                                     -> PomodoroType.SHORT_BREAK
        }

        _uiState.update {
            it.copy(
                activeSessionId = null,
                cycleIndex      = nextCycle,
                selectedType    = nextType,
                timerState      = TimerState.Idle
            )
        }
        _snackbar.tryEmit(
            when (state.selectedType) {
                PomodoroType.FOCUS       -> "Focus session complete! 🎉"
                PomodoroType.SHORT_BREAK -> "Break over — time to focus!"
                PomodoroType.LONG_BREAK  -> "Long break done — great job!"
            }
        )
        refreshStats()
    }

    private fun refreshStats() {
        viewModelScope.launch {
            getTodayStatsUseCase().let { result ->
                if (result is com.oussama_chatri.productivityx.core.util.Resource.Success) {
                    _uiState.update { it.copy(todayStats = result.data) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
