package com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ReorderTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.RefreshTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.SoftDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TasksEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TasksUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.filterForTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val updateStatus: UpdateTaskStatusUseCase,
    private val softDelete: SoftDeleteTaskUseCase,
    private val reorderTasks: ReorderTasksUseCase,
    private val refreshTasks: RefreshTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTasks()
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.SelectTab -> _uiState.update { it.copy(activeTab = event.tab) }

            is TasksEvent.ToggleView -> _uiState.update { it.copy(viewMode = event.view) }

            is TasksEvent.FilterByStatus -> {
                _uiState.update { it.copy(filterStatus = event.status, filterPriority = null) }
                loadTasks()
            }

            is TasksEvent.FilterByPriority -> {
                _uiState.update { it.copy(filterPriority = event.priority, filterStatus = null) }
                loadTasks()
            }

            is TasksEvent.CompleteTask -> viewModelScope.launch {
                when (val result = updateStatus(event.taskId, TaskStatus.DONE)) {
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TasksEvent.DeleteTask -> viewModelScope.launch {
                when (val result = softDelete(event.taskId)) {
                    is Resource.Success -> _uiEvent.send(
                        UiEvent.ShowSnackbar(
                            message = "Task deleted",
                            actionLabel = "Undo",
                            onAction = { onEvent(TasksEvent.UndoDelete(event.taskId)) }
                        )
                    )
                    is Resource.Error -> _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }

            is TasksEvent.UndoDelete -> viewModelScope.launch {
                // Undo is handled by the restore mechanism in TaskDetailViewModel / direct call
                _uiEvent.send(UiEvent.ShowSnackbar("Undo not implemented yet"))
            }

            is TasksEvent.ReorderTasks -> viewModelScope.launch {
                reorderTasks(event.items)
            }

            is TasksEvent.Refresh -> viewModelScope.launch {
                _uiState.update { it.copy(isSyncing = true) }
                refreshTasks()
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    private fun loadTasks() {
        val state = _uiState.value
        observeTasks(
            status = state.filterStatus,
            priority = state.filterPriority
        )
            .onEach { tasks ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        tasks = tasks.filterForTab(current.activeTab),
                        error = null
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }
}
