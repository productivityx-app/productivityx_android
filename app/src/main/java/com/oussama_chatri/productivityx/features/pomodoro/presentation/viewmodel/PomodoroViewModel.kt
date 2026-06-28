package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import com.oussama_chatri.productivityx.features.pomodoro.service.PomodoroForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private fun Context.startForegroundServiceCompat(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTodayStatsUseCase: GetTodayStatsUseCase,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    private var tickJob: Job? = null
    private var tickStartEpochMs: Long = 0L

    init {
        refreshStats()
        recoverTimer()
    }

    private fun recoverTimer() {
        viewModelScope.launch {
            val sessionId = preferencesDataStore.pomodoroSessionId.first() ?: return@launch
            val typeName = preferencesDataStore.pomodoroType.first() ?: return@launch
            val totalSeconds = preferencesDataStore.pomodoroTotalSeconds.first()
            val startEpochMs = preferencesDataStore.pomodoroStartEpochMs.first()
            val taskId = preferencesDataStore.pomodoroTaskId.first()
            val taskTitle = preferencesDataStore.pomodoroTaskTitle.first()
            val cycleIndex = preferencesDataStore.pomodoroCycleIndex.first()
            val isPaused = preferencesDataStore.pomodoroIsPaused.first()
            val pausedRemaining = preferencesDataStore.pomodoroPausedRemainingSeconds.first()

            if (startEpochMs == 0L) return@launch

            val type = try { PomodoroType.valueOf(typeName) } catch (_: Exception) { return@launch }
            val elapsedSecs = ((System.currentTimeMillis() - startEpochMs) / 1000).toInt()
            val remainingSeconds = maxOf(0, totalSeconds - elapsedSecs)

            if (remainingSeconds <= 0) {
                preferencesDataStore.clearPomodoroTimer()
                return@launch
            }

            _uiState.update {
                it.copy(
                    activeSessionId = sessionId,
                    selectedType = type,
                    cycleIndex = cycleIndex,
                    linkedTaskId = taskId,
                    linkedTaskTitle = taskTitle,
                    timerState = if (isPaused) {
                        TimerState.Paused(
                            sessionId = sessionId,
                            type = type,
                            totalSeconds = totalSeconds,
                            remainingSeconds = pausedRemaining,
                            taskId = taskId,
                            taskTitle = taskTitle,
                            cycleIndex = cycleIndex
                        )
                    } else {
                        TimerState.Running(
                            sessionId = sessionId,
                            type = type,
                            totalSeconds = totalSeconds,
                            remainingSeconds = remainingSeconds,
                            taskId = taskId,
                            taskTitle = taskTitle,
                            cycleIndex = cycleIndex
                        )
                    }
                )
            }

            if (!isPaused) {
                startTicking(sessionId, type, totalSeconds, taskId, taskTitle, cycleIndex)
            }

            context.startForegroundServiceCompat(
                PomodoroForegroundService.startIntent(
                    context = context,
                    sessionId = sessionId,
                    type = type,
                    totalSeconds = totalSeconds,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    cycleIndex = cycleIndex
                )
            )
        }
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
        if (!state.isIdle) return

        val sessionId = UUID.randomUUID().toString()
        val totalSecs = state.totalSeconds
        val nowMs = System.currentTimeMillis()

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

        viewModelScope.launch {
            preferencesDataStore.savePomodoroTimer(
                sessionId = sessionId,
                type = state.selectedType.name,
                totalSeconds = totalSecs,
                startEpochMs = nowMs,
                taskId = state.linkedTaskId,
                taskTitle = state.linkedTaskTitle,
                cycleIndex = state.cycleIndex
            )
        }

        context.startForegroundServiceCompat(
            PomodoroForegroundService.startIntent(
                context = context,
                sessionId = sessionId,
                type = state.selectedType,
                totalSeconds = totalSecs,
                taskId = state.linkedTaskId,
                taskTitle = state.linkedTaskTitle,
                cycleIndex = state.cycleIndex
            )
        )

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
        tickStartEpochMs = System.currentTimeMillis()
        tickJob = viewModelScope.launch {
            while (true) {
                val elapsedMs = System.currentTimeMillis() - tickStartEpochMs
                val remaining = maxOf(0, totalSeconds - (elapsedMs / 1000).toInt())
                val current = _uiState.value.timerState

                when (current) {
                    is TimerState.Completed, TimerState.Idle -> break
                    is TimerState.Paused -> {
                        val nextSleep = 200L
                        delay(nextSleep)
                        continue
                    }
                    is TimerState.Running -> {
                        _uiState.update {
                            it.copy(timerState = current.copy(remainingSeconds = remaining))
                        }
                        if (remaining <= 0) {
                            _uiState.update {
                                it.copy(
                                    timerState = TimerState.Completed(type, cycleIndex)
                                )
                            }
                            viewModelScope.launch { preferencesDataStore.clearPomodoroTimer() }
                            onEvent(PomodoroUiEvent.SessionCompleted)
                            break
                        }
                        val nextBoundaryMs = ((elapsedMs / 1000) + 1) * 1000
                        val sleepMs = nextBoundaryMs - elapsedMs
                        if (sleepMs > 0) delay(sleepMs)
                    }
                }
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
        viewModelScope.launch {
            preferencesDataStore.savePomodoroPaused(current.remainingSeconds)
        }
        context.startService(PomodoroForegroundService.pauseIntent(context))
    }

    private fun resumeTimer() {
        val current = _uiState.value.timerState as? TimerState.Paused ?: return
        tickStartEpochMs = System.currentTimeMillis() - (current.totalSeconds - current.remainingSeconds) * 1000L
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
        viewModelScope.launch {
            preferencesDataStore.savePomodoroResumed()
        }
        context.startService(PomodoroForegroundService.resumeIntent(context))
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
        viewModelScope.launch {
            preferencesDataStore.clearPomodoroTimer()
        }
        context.startService(PomodoroForegroundService.skipIntent(context))
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
        viewModelScope.launch {
            preferencesDataStore.clearPomodoroTimer()
        }
        context.startService(PomodoroForegroundService.stopIntent(context))
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
                PomodoroType.FOCUS       -> "Focus session complete!"
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
