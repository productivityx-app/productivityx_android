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
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskUseCase
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

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskById: GetTaskByIdUseCase,
    private val updateStatus: UpdateTaskStatusUseCase,
    private val updateTask: UpdateTaskUseCase,
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
                    is Resource.Success -> {
                        _uiState.update { it.copy(task = result.data) }
                        if (event.status == com.oussama_chatri.productivityx.core.enums.TaskStatus.DONE) {
                            _uiState.update { it.copy(showCelebration = true, showConfetti = isMilestoneTask(result.data)) }
                        }
                    }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.CompleteTask -> viewModelScope.launch {
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

            // Inline editing - Title
            is TaskDetailEvent.StartEditTitle -> {
                _uiState.update { it.copy(isEditingTitle = true, editingTitle = _uiState.value.task?.title ?: "") }
            }
            is TaskDetailEvent.CancelEditTitle -> {
                _uiState.update { it.copy(isEditingTitle = false, editingTitle = "") }
            }
            is TaskDetailEvent.TitleChanged -> {
                _uiState.update { it.copy(editingTitle = event.title) }
            }
            is TaskDetailEvent.SaveTitle -> viewModelScope.launch {
                val title = _uiState.value.editingTitle.trim()
                if (title.isNotBlank()) {
                    when (val result = updateTask(taskId, title = title)) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(task = result.data, isEditingTitle = false, editingTitle = "") }
                        }
                        is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                        else -> Unit
                    }
                }
            }

            // Inline editing - Description
            is TaskDetailEvent.StartEditDescription -> {
                _uiState.update { it.copy(isEditingDescription = true, editingDescription = _uiState.value.task?.description ?: "") }
            }
            is TaskDetailEvent.CancelEditDescription -> {
                _uiState.update { it.copy(isEditingDescription = false, editingDescription = "") }
            }
            is TaskDetailEvent.DescriptionChanged -> {
                _uiState.update { it.copy(editingDescription = event.description) }
            }
            is TaskDetailEvent.SaveDescription -> viewModelScope.launch {
                val description = _uiState.value.editingDescription.trim().ifBlank { null }
                when (val result = updateTask(taskId, description = description)) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(task = result.data, isEditingDescription = false, editingDescription = "") }
                    }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            // Subtask inline - requires CreateTaskUseCase injection
            is TaskDetailEvent.AddSubtaskInline -> viewModelScope.launch {
                loadTask()
            }

            is TaskDetailEvent.ToggleSubtask -> viewModelScope.launch {
                val subtask = _uiState.value.task?.subtasks?.find { it.id == event.subtaskId } ?: return@launch
                val newStatus = if (subtask.status == com.oussama_chatri.productivityx.core.enums.TaskStatus.DONE)
                    com.oussama_chatri.productivityx.core.enums.TaskStatus.TODO
                else
                    com.oussama_chatri.productivityx.core.enums.TaskStatus.DONE
                updateStatus(event.subtaskId, newStatus)
                loadTask()
            }

            // Scheduling updates
            is TaskDetailEvent.UpdateDueDate -> viewModelScope.launch {
                when (val result = updateTask(taskId, dueDate = event.date)) {
                    is Resource.Success -> _uiState.update { it.copy(task = result.data) }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.UpdateDueTime -> viewModelScope.launch {
                when (val result = updateTask(taskId, dueTime = event.time)) {
                    is Resource.Success -> _uiState.update { it.copy(task = result.data) }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.UpdateReminder -> viewModelScope.launch {
                when (val result = updateTask(taskId, reminderAt = event.instant)) {
                    is Resource.Success -> _uiState.update { it.copy(task = result.data) }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.UpdatePriority -> viewModelScope.launch {
                when (val result = updateTask(taskId, priority = event.priority)) {
                    is Resource.Success -> _uiState.update { it.copy(task = result.data) }
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TaskDetailEvent.DismissCelebration -> {
                _uiState.update { it.copy(showCelebration = false, showConfetti = false) }
            }
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

    private fun isMilestoneTask(task: com.oussama_chatri.productivityx.features.tasks.domain.model.Task?): Boolean {
        return task?.hasSubtasks == true || task?.priority == com.oussama_chatri.productivityx.core.enums.Priority.URGENT
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
