package com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.HardDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTaskTrashUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.RefreshTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.RestoreTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.SoftDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TaskDetailEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TaskTrashEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskDetailUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTrashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── TaskDetailViewModel ──────────────────────────────────────────────────────

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskById: GetTaskByIdUseCase,
    private val updateStatus: UpdateTaskStatusUseCase,
    private val softDelete: SoftDeleteTaskUseCase,
    private val restore: RestoreTaskUseCase
) : ViewModel() {

    private val taskId: String = requireNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTask()
    }

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.StatusChanged -> viewModelScope.launch {
                when (val result = updateStatus(taskId, event.status)) {
                    is Resource.Success -> _uiState.update { it.copy(task = result.data) }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.CompleteTask -> viewModelScope.launch {
                val task = _uiState.value.task ?: return@launch
                onEvent(TaskDetailEvent.StatusChanged(
                    com.oussama_chatri.productivityx.core.enums.TaskStatus.DONE
                ))
            }

            is TaskDetailEvent.DeleteTask -> viewModelScope.launch {
                when (val result = softDelete(taskId)) {
                    is Resource.Success -> {
                        _uiEvent.send(UiEvent.ShowSnackbar("Task moved to trash"))
                        _uiEvent.send(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.RestoreTask -> viewModelScope.launch {
                when (val result = restore(taskId)) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(task = result.data) }
                        _uiEvent.send(UiEvent.ShowSnackbar("Task restored"))
                    }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.Refresh -> loadTask()
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getTaskById(taskId)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, task = result.data, error = null) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }
}

// ─── TaskTrashViewModel ───────────────────────────────────────────────────────

@HiltViewModel
class TaskTrashViewModel @Inject constructor(
    private val observeTrash: ObserveTaskTrashUseCase,
    private val restore: RestoreTaskUseCase,
    private val hardDelete: HardDeleteTaskUseCase,
    private val refreshTasks: RefreshTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskTrashUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeTrash()
            .onEach { tasks -> _uiState.update { it.copy(isLoading = false, tasks = tasks) } }
            .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: TaskTrashEvent) {
        when (event) {
            is TaskTrashEvent.RestoreTask -> viewModelScope.launch {
                when (val result = restore(event.taskId)) {
                    is Resource.Success -> _uiEvent.send(UiEvent.ShowSnackbar("Task restored"))
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskTrashEvent.HardDeleteTask -> viewModelScope.launch {
                when (val result = hardDelete(event.taskId)) {
                    is Resource.Success -> _uiEvent.send(UiEvent.ShowSnackbar("Task permanently deleted"))
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskTrashEvent.Refresh -> viewModelScope.launch {
                refreshTasks()
            }
        }
    }
}
