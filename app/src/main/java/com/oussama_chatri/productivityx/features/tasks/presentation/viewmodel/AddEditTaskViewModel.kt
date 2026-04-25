package com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.CreateTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.GetTaskByIdUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.HardDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.SoftDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.presentation.event.AddEditTaskEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.AddEditTaskUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskById: GetTaskByIdUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val softDeleteTask: SoftDeleteTaskUseCase,
    private val hardDeleteTask: HardDeleteTaskUseCase
) : ViewModel() {

    private val taskId: String?     = savedStateHandle["taskId"]
    private val parentTaskId: String? = savedStateHandle["parentTaskId"]

    private val _uiState = MutableStateFlow(
        AddEditTaskUiState(taskId = taskId, parentTaskId = parentTaskId)
    )
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        if (taskId != null) loadExistingTask(taskId)
    }

    fun onEvent(event: AddEditTaskEvent) {
        when (event) {
            is AddEditTaskEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.value, titleError = null) }

            is AddEditTaskEvent.DescriptionChanged ->
                _uiState.update { it.copy(description = event.value) }

            is AddEditTaskEvent.StatusChanged ->
                _uiState.update { it.copy(status = event.status) }

            is AddEditTaskEvent.PriorityChanged ->
                _uiState.update { it.copy(priority = event.priority) }

            is AddEditTaskEvent.DueDateChanged ->
                _uiState.update { it.copy(dueDate = event.date) }

            is AddEditTaskEvent.DueTimeChanged ->
                _uiState.update { it.copy(dueTime = event.time) }

            is AddEditTaskEvent.ReminderChanged ->
                _uiState.update { it.copy(reminderAt = event.instant) }

            is AddEditTaskEvent.EstimatedMinutesChanged ->
                _uiState.update { it.copy(estimatedMinutes = event.minutes) }

            is AddEditTaskEvent.NewSubtaskTitleChanged ->
                _uiState.update { it.copy(newSubtaskTitle = event.title) }

            is AddEditTaskEvent.AddSubtask    -> addSubtask()
            is AddEditTaskEvent.RemoveSubtask ->
                _uiState.update { s -> s.copy(subtasks = s.subtasks.filter { it.id != event.subtaskId }) }

            is AddEditTaskEvent.Save          -> save()
            is AddEditTaskEvent.Delete        -> delete()
            is AddEditTaskEvent.ClearError    ->
                _uiState.update { it.copy(titleError = null) }
        }
    }

    private fun loadExistingTask(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getTaskById(id)) {
                is Resource.Success -> {
                    val task = result.data
                    _uiState.update {
                        it.copy(
                            isLoading        = false,
                            title            = task.title,
                            description      = task.description ?: "",
                            status           = task.status,
                            priority         = task.priority,
                            dueDate          = task.dueDate,
                            dueTime          = task.dueTime,
                            reminderAt       = task.reminderAt,
                            estimatedMinutes = task.estimatedMinutes,
                            linkedEventId    = task.linkedEventId,
                            subtasks         = task.subtasks
                        )
                    }
                }
                // AddEditTaskUiState has no generic error field — surface via snackbar
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                }
                else -> Unit
            }
        }
    }

    private fun addSubtask() {
        val title = _uiState.value.newSubtaskTitle.trim()
        if (title.isBlank()) return

        viewModelScope.launch {
            val parentId = taskId ?: return@launch
            when (val result = createTask(title = title, parentTaskId = parentId)) {
                is Resource.Success -> {
                    _uiState.update { s ->
                        s.copy(subtasks = s.subtasks + result.data, newSubtaskTitle = "")
                    }
                }
                is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                else              -> Unit
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (state.isEditMode) {
                updateTask(
                    taskId           = state.taskId!!,
                    title            = state.title,
                    description      = state.description.ifBlank { null },
                    status           = state.status,
                    priority         = state.priority,
                    dueDate          = state.dueDate,
                    dueTime          = state.dueTime,
                    reminderAt       = state.reminderAt,
                    estimatedMinutes = state.estimatedMinutes,
                    linkedEventId    = state.linkedEventId
                )
            } else {
                createTask(
                    title            = state.title,
                    description      = state.description.ifBlank { null },
                    status           = state.status,
                    priority         = state.priority,
                    dueDate          = state.dueDate,
                    dueTime          = state.dueTime,
                    reminderAt       = state.reminderAt,
                    estimatedMinutes = state.estimatedMinutes,
                    parentTaskId     = state.parentTaskId,
                    linkedEventId    = state.linkedEventId
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    _uiEvent.send(UiEvent.NavigateBack)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun delete() {
        val id = taskId ?: return
        viewModelScope.launch {
            when (val result = softDeleteTask(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDeleted = true) }
                    _uiEvent.send(UiEvent.ShowSnackbar("Task deleted"))
                    _uiEvent.send(UiEvent.NavigateBack)
                }
                is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                else              -> Unit
            }
        }
    }
}