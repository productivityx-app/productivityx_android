package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTasksUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.AmbientSound
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import com.oussama_chatri.productivityx.features.pomodoro.service.PomodoroForegroundService
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskUseCase
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val preferencesDataStore: PreferencesDataStore,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val pomodoroRepository: PomodoroRepository,
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val ambientSoundManager: com.oussama_chatri.productivityx.features.pomodoro.service.AmbientSoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    private val _tasks = MutableStateFlow<List<com.oussama_chatri.productivityx.features.tasks.domain.model.Task>>(emptyList())
    val tasks: StateFlow<List<com.oussama_chatri.productivityx.features.tasks.domain.model.Task>> = _tasks.asStateFlow()

    private var tickJob: Job? = null
    private var tickStartEpochMs = 0L

    init {
        recoverTimer()
        refreshStats()
        observeTasksUseCase()
            .map { it.filter { task -> !task.isDeleted && task.status != TaskStatus.DONE && task.status != TaskStatus.CANCELLED } }
            .onEach { _tasks.value = it }
            .launchIn(viewModelScope)
            
        preferencesDataStore.pomodoroBackgroundImageUri
            .onEach { uri -> _uiState.update { it.copy(backgroundImageUri = uri) } }
            .launchIn(viewModelScope)
    }

    private fun recoverTimer() {
        viewModelScope.launch {
            val sessionId = preferencesDataStore.pomodoroSessionId.first()
            if (sessionId != null) {
                val type = PomodoroType.valueOf(preferencesDataStore.pomodoroType.first() ?: "FOCUS")
                val totalSecs = preferencesDataStore.pomodoroTotalSeconds.first()
                val startMs = preferencesDataStore.pomodoroStartEpochMs.first()
                val taskId = preferencesDataStore.pomodoroTaskId.first()
                val taskTitle = preferencesDataStore.pomodoroTaskTitle.first()
                val cycleIdx = preferencesDataStore.pomodoroCycleIndex.first()
                val isPaused = preferencesDataStore.pomodoroIsPaused.first()
                val pausedRem = preferencesDataStore.pomodoroPausedRemainingSeconds.first()

                if (isPaused) {
                    _uiState.update { it.copy(
                        timerState = TimerState.Paused(sessionId, type, totalSecs, pausedRem, taskId, taskTitle, cycleIdx),
                        selectedType = type,
                        linkedTaskId = taskId,
                        linkedTaskTitle = taskTitle,
                        cycleIndex = cycleIdx
                    ) }
                } else {
                    val now = System.currentTimeMillis()
                    val elapsed = ((now - startMs) / 1000).toInt()
                    val remaining = (totalSecs - elapsed).coerceAtLeast(0)

                    if (remaining > 0) {
                        _uiState.update { it.copy(
                            timerState = TimerState.Running(sessionId, type, totalSecs, remaining, taskId, taskTitle, cycleIdx),
                            selectedType = type,
                            linkedTaskId = taskId,
                            linkedTaskTitle = taskTitle,
                            cycleIndex = cycleIdx
                        ) }
                        startTicking(sessionId, type, totalSecs, taskId, taskTitle, cycleIdx)
                    } else {
                        // Timer finished while app was closed
                        onSessionCompleted()
                    }
                }
            }
        }
    }

    fun onEvent(event: PomodoroUiEvent) {
        when (event) {
            is PomodoroUiEvent.SelectType -> _uiState.update { it.copy(selectedType = event.type) }
            PomodoroUiEvent.StartSession -> startSession()
            PomodoroUiEvent.PauseTimer -> pauseTimer()
            PomodoroUiEvent.ResumeTimer -> resumeTimer()
            PomodoroUiEvent.SkipTimer -> skipTimer()
            PomodoroUiEvent.StopAndInterrupt -> _uiState.update { it.copy(showInterruptDialog = true) }
            is PomodoroUiEvent.ConfirmInterrupt -> confirmInterrupt(event.reason)
            PomodoroUiEvent.DismissInterruptDialog -> _uiState.update { it.copy(showInterruptDialog = false) }
            PomodoroUiEvent.ShowTaskPicker -> _uiState.update { it.copy(showTaskPickerSheet = true) }
            is PomodoroUiEvent.SelectTask -> _uiState.update { it.copy(linkedTaskId = event.taskId, linkedTaskTitle = event.title, showTaskPickerSheet = false) }
            PomodoroUiEvent.UnlinkTask -> _uiState.update { it.copy(linkedTaskId = null, linkedTaskTitle = null) }
            
            // New Events
            PomodoroUiEvent.ToggleFocusMode -> {
                val newFocusMode = !_uiState.value.isFocusMode
                _uiState.update { it.copy(isFocusMode = newFocusMode) }
                if (newFocusMode) {
                    _uiState.update { it.copy(motivationalQuote = getRandomQuote()) }
                }
            }
            is PomodoroUiEvent.SelectAmbientSound -> {
                _uiState.update { it.copy(selectedAmbientSound = event.sound) }
                if (_uiState.value.isFocusMode || _uiState.value.isRunning) {
                    ambientSoundManager.playSound(event.sound)
                }
            }
            PomodoroUiEvent.ToggleDnd -> toggleDnd()
            PomodoroUiEvent.Extend1Min -> extendTimer(60)
            is PomodoroUiEvent.SelectBackground -> {
                viewModelScope.launch {
                    preferencesDataStore.setPomodoroBackgroundImageUri(null)
                    preferencesDataStore.setPomodoroBackgroundImageUri(event.uri)
                }
            }
        }
    }

    private fun startSession() {
        val type = _uiState.value.selectedType
        val totalSecs = _uiState.value.totalSeconds
        val taskId = _uiState.value.linkedTaskId
        val taskTitle = _uiState.value.linkedTaskTitle
        val cycleIdx = _uiState.value.cycleIndex

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStart = true) }

            val result = pomodoroRepository.startSession(type, taskId)
            val sessionId = when (result) {
                is com.oussama_chatri.productivityx.core.util.Resource.Success -> result.data.id
                else -> UUID.randomUUID().toString()
            }

            preferencesDataStore.savePomodoroTimer(sessionId, type.name, totalSecs, System.currentTimeMillis(), taskId, taskTitle, cycleIdx)

            _uiState.update { it.copy(
                timerState = TimerState.Running(sessionId, type, totalSecs, totalSecs, taskId, taskTitle, cycleIdx),
                isLoadingStart = false,
                activeSessionId = sessionId
            ) }

            playSoundIfNeeded()

            val intent = PomodoroForegroundService.startIntent(context, sessionId, type, totalSecs, taskId, taskTitle, cycleIdx)
            context.startForegroundServiceCompat(intent)

            startTicking(sessionId, type, totalSecs, taskId, taskTitle, cycleIdx)
        }
    }

    private fun startTicking(sessionId: String, type: PomodoroType, totalSecs: Int, taskId: String?, taskTitle: String?, cycleIdx: Int) {
        tickJob?.cancel()
        tickStartEpochMs = System.currentTimeMillis()
        val initialRemaining = (uiState.value.timerState as? TimerState.Running)?.remainingSeconds ?: totalSecs
        
        tickJob = viewModelScope.launch {
            var remaining = initialRemaining
            while (remaining > 0) {
                delay(1000)
                val now = System.currentTimeMillis()
                val elapsed = ((now - tickStartEpochMs) / 1000).toInt()
                remaining = (initialRemaining - elapsed).coerceAtLeast(0)
                
                _uiState.update { state ->
                    if (state.timerState is TimerState.Running) {
                        state.copy(timerState = state.timerState.copy(remainingSeconds = remaining))
                    } else state
                }
            }
            onSessionCompleted()
        }
    }

    private fun pauseTimer() {
        tickJob?.cancel()
        val current = _uiState.value.timerState as? TimerState.Running ?: return

        ambientSoundManager.stopSound()
        
        viewModelScope.launch {
            preferencesDataStore.savePomodoroPaused(current.remainingSeconds)
            _uiState.update { it.copy(
                timerState = TimerState.Paused(
                    current.sessionId, current.type, current.totalSeconds, 
                    current.remainingSeconds, current.taskId, current.taskTitle, current.cycleIndex
                )
            ) }
            context.startService(PomodoroForegroundService.pauseIntent(context))
        }
    }

    private fun resumeTimer() {
        val current = _uiState.value.timerState as? TimerState.Paused ?: return

        playSoundIfNeeded()
        
        viewModelScope.launch {
            preferencesDataStore.savePomodoroResumed()
            _uiState.update { it.copy(
                timerState = TimerState.Running(
                    current.sessionId, current.type, current.totalSeconds, 
                    current.remainingSeconds, current.taskId, current.taskTitle, current.cycleIndex
                )
            ) }
            context.startService(PomodoroForegroundService.resumeIntent(context))
            startTicking(current.sessionId, current.type, current.totalSeconds, current.taskId, current.taskTitle, current.cycleIndex)
        }
    }

    private fun skipTimer() {
        tickJob?.cancel()
        ambientSoundManager.stopSound()
        viewModelScope.launch {
            context.startService(PomodoroForegroundService.skipIntent(context))
            onSessionCompleted()
        }
    }

    private fun confirmInterrupt(reason: String) {
        tickJob?.cancel()
        ambientSoundManager.stopSound()
        val current = _uiState.value.timerState
        val sessionId = when(current) {
            is TimerState.Running -> current.sessionId
            is TimerState.Paused -> current.sessionId
            else -> null
        }

        viewModelScope.launch {
            if (sessionId != null) {
                pomodoroRepository.interruptSession(sessionId, null, reason)
            }
            preferencesDataStore.clearPomodoroTimer()
            context.startService(PomodoroForegroundService.stopIntent(context))
            _uiState.update { it.copy(
                timerState = TimerState.Idle,
                showInterruptDialog = false,
                interruptReason = ""
            ) }
            refreshStats()
        }
    }

    private fun onSessionCompleted() {
        ambientSoundManager.stopSound()
        val type = _uiState.value.selectedType
        val taskId = _uiState.value.linkedTaskId
        val sessionId = when (val t = _uiState.value.timerState) {
            is TimerState.Running -> t.sessionId
            is TimerState.Paused -> t.sessionId
            else -> null
        }

        viewModelScope.launch {
            if (type == PomodoroType.FOCUS && taskId != null) {
                updateTaskStatusUseCase(taskId, TaskStatus.DONE)
                updateTaskUseCase(taskId, description = "Completed via Pomodoro")
            }

            if (sessionId != null) {
                pomodoroRepository.endSession(sessionId, _uiState.value.totalSeconds)
            }

            val nextCycleIdx = if (type == PomodoroType.FOCUS) _uiState.value.cycleIndex + 1 else _uiState.value.cycleIndex
            val nextType = when {
                type == PomodoroType.FOCUS && nextCycleIdx % _uiState.value.cyclesBeforeLongBreak == 0 -> PomodoroType.LONG_BREAK
                type == PomodoroType.FOCUS -> PomodoroType.SHORT_BREAK
                else -> PomodoroType.FOCUS
            }

            preferencesDataStore.clearPomodoroTimer()
            _uiState.update { it.copy(
                timerState = TimerState.Completed(type, nextCycleIdx),
                selectedType = nextType,
                cycleIndex = nextCycleIdx
            ) }
            refreshStats()
        }
    }
    
    private fun extendTimer(seconds: Int) {
        val current = _uiState.value.timerState
        if (current is TimerState.Running) {
            val newRemaining = current.remainingSeconds + seconds
            val newTotal = current.totalSeconds + seconds
            _uiState.update { it.copy(
                timerState = current.copy(remainingSeconds = newRemaining, totalSeconds = newTotal)
            ) }
            // Update start epoch so ticking remains accurate
            tickStartEpochMs += seconds * 1000
        } else if (current is TimerState.Paused) {
            val newRemaining = current.remainingSeconds + seconds
            val newTotal = current.totalSeconds + seconds
            _uiState.update { it.copy(
                timerState = current.copy(remainingSeconds = newRemaining, totalSeconds = newTotal)
            ) }
        }
    }
    
    private fun toggleDnd() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val filter = if (_uiState.value.isDndEnabled) NotificationManager.INTERRUPTION_FILTER_ALL else NotificationManager.INTERRUPTION_FILTER_PRIORITY
                notificationManager.setInterruptionFilter(filter)
                _uiState.update { it.copy(isDndEnabled = !it.isDndEnabled) }
            } else {
                viewModelScope.launch {
                    _snackbar.emit("DND Access required. Please enable in settings.")
                }
            }
        }
    }

    private fun playSoundIfNeeded() {
        val sound = _uiState.value.selectedAmbientSound
        if (sound != AmbientSound.NONE) {
            ambientSoundManager.playSound(sound)
        }
    }

    private fun refreshStats() {
        viewModelScope.launch {
            val result = getTodayStatsUseCase()
            if (result is com.oussama_chatri.productivityx.core.util.Resource.Success) {
                _uiState.update { it.copy(
                    todayStats = result.data,
                    completedFocusMinutesToday = (result.data?.totalFocusMinutesToday ?: 0L).toInt()
                ) }
            }
        }
    }

    private fun getRandomQuote(): String {
        val quotes = listOf(
            "Focus on being productive instead of busy.",
            "The only way to do great work is to love what you do.",
            "Don't stop until you're proud.",
            "Your mind is for having ideas, not holding them.",
            "Productivity is being able to do things that you were never able to do before.",
            "Small progress is still progress."
        )
        return quotes.random()
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
        ambientSoundManager.release()
    }
}
