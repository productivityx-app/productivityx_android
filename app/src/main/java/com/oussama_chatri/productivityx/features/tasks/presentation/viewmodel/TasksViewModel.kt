package com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.model.TaskFilter
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.CreateTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.GetPagedTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.RefreshTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ReorderTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.SoftDeleteTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.UpdateTaskUseCase
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TasksEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TasksUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.applyFilter
import com.oussama_chatri.productivityx.features.tasks.presentation.state.filterForTab
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
class TasksViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val getPagedTasks: GetPagedTasksUseCase,
    private val updateStatus: UpdateTaskStatusUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val softDelete: SoftDeleteTaskUseCase,
    private val reorderTasks: ReorderTasksUseCase,
    private val createTask: CreateTaskUseCase,
    private val refreshTasks: RefreshTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _pagedTasks = MutableStateFlow<PagingData<Task>>(PagingData.empty())
    val pagedTasks = _pagedTasks.asStateFlow()

    init {
        loadTasks()
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.SelectTab -> {
                _uiState.update { it.copy(activeTab = event.tab) }
                reapplyFilter()
            }

            is TasksEvent.ToggleView -> _uiState.update { it.copy(viewMode = event.view) }

            is TasksEvent.FilterByStatus -> {
                _uiState.update { it.copy(filterStatus = event.status, filterPriority = null) }
                reapplyFilter()
            }

            is TasksEvent.FilterByPriority -> {
                _uiState.update { it.copy(filterPriority = event.priority, filterStatus = null) }
                reapplyFilter()
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

            is TasksEvent.UndoDelete -> {} // Handled via snackbar action

            is TasksEvent.ReorderTasks -> viewModelScope.launch {
                reorderTasks(event.items)
            }

            is TasksEvent.Refresh -> viewModelScope.launch {
                _uiState.update { it.copy(isSyncing = true) }
                refreshTasks()
                _uiState.update { it.copy(isSyncing = false) }
            }

            // Smart filter
            is TasksEvent.SetSmartFilter -> {
                _uiState.update { it.copy(taskFilter = it.taskFilter.copy(smartFilter = event.filter)) }
                reapplyFilter()
            }

            is TasksEvent.SetTagFilter -> {
                _uiState.update { it.copy(taskFilter = it.taskFilter.copy(tagFilter = event.tag)) }
                reapplyFilter()
            }

            is TasksEvent.SetSearchQuery -> {
                _uiState.update { it.copy(taskFilter = it.taskFilter.copy(searchQuery = event.query)) }
                reapplyFilter()
            }

            is TasksEvent.SetCustomDateRange -> {
                _uiState.update {
                    it.copy(taskFilter = it.taskFilter.copy(
                        customStartDate = event.start,
                        customEndDate = event.end
                    ))
                }
                reapplyFilter()
            }

            // Multi-select
            is TasksEvent.ToggleTaskSelection -> {
                _uiState.update { state ->
                    val newSelected = if (state.selectedTaskIds.contains(event.taskId)) {
                        state.selectedTaskIds - event.taskId
                    } else {
                        state.selectedTaskIds + event.taskId
                    }
                    state.copy(
                        selectedTaskIds = newSelected,
                        isMultiSelectMode = newSelected.isNotEmpty()
                    )
                }
            }

            is TasksEvent.SelectAll -> {
                _uiState.update { state ->
                    val allIds = state.filteredTasks.map { it.id }.toSet()
                    state.copy(selectedTaskIds = allIds, isMultiSelectMode = allIds.isNotEmpty())
                }
            }

            is TasksEvent.ClearSelection -> {
                _uiState.update { it.copy(selectedTaskIds = emptySet(), isMultiSelectMode = false) }
            }

            is TasksEvent.EnterMultiSelectMode -> {
                _uiState.update { it.copy(isMultiSelectMode = true) }
            }

            is TasksEvent.ExitMultiSelectMode -> {
                _uiState.update { it.copy(isMultiSelectMode = false, selectedTaskIds = emptySet()) }
            }

            // Bulk actions
            is TasksEvent.BulkComplete -> bulkComplete()
            is TasksEvent.BulkDelete -> bulkDelete()
            is TasksEvent.BulkReschedule -> bulkReschedule(event.date)
            is TasksEvent.BulkSetPriority -> bulkSetPriority(event.priority)
            is TasksEvent.BulkAddTag -> bulkAddTag(event.tag)

            // Calendar
            is TasksEvent.CalendarNavigateMonth -> {
                _uiState.update { state ->
                    state.copy(calendarStartDate = state.calendarStartDate.plusMonths(event.delta.toLong()))
                }
            }

            is TasksEvent.SelectCalendarDate -> {
                _uiState.update { it.copy(calendarSelectedDate = event.date) }
            }

            // Timeline
            is TasksEvent.TimelineZoomIn -> {
                _uiState.update { state ->
                    state.copy(
                        timelineStartDate = state.timelineStartDate.plusDays(event.days.toLong()),
                        timelineEndDate = state.timelineEndDate.minusDays(event.days.toLong())
                    )
                }
            }

            is TasksEvent.TimelineZoomOut -> {
                _uiState.update { state ->
                    state.copy(
                        timelineStartDate = state.timelineStartDate.minusDays(event.days.toLong()),
                        timelineEndDate = state.timelineEndDate.plusDays(event.days.toLong())
                    )
                }
            }

            is TasksEvent.TimelinePan -> {
                _uiState.update { state ->
                    state.copy(
                        timelineStartDate = state.timelineStartDate.plusDays(event.delta.toLong()),
                        timelineEndDate = state.timelineEndDate.plusDays(event.delta.toLong())
                    )
                }
            }

            // Drag-drop
            is TasksEvent.MoveTaskToStatus -> viewModelScope.launch {
                updateStatus(event.taskId, event.status)
            }

            is TasksEvent.MoveTaskToPosition -> viewModelScope.launch {
                reorderTasks(listOf(event.taskId to event.position))
            }
        }
    }

    private fun bulkComplete() = viewModelScope.launch {
        _uiState.update { it.copy(isBulkActionRunning = true) }
        val ids = _uiState.value.selectedTaskIds.toList()
        ids.forEach { id -> updateStatus(id, TaskStatus.DONE) }
        _uiState.update { it.copy(isBulkActionRunning = false, selectedTaskIds = emptySet(), isMultiSelectMode = false) }
        _uiEvent.send(UiEvent.ShowSnackbar("${ids.size} tasks completed"))
    }

    private fun bulkDelete() = viewModelScope.launch {
        _uiState.update { it.copy(isBulkActionRunning = true) }
        val ids = _uiState.value.selectedTaskIds.toList()
        ids.forEach { id -> softDelete(id) }
        _uiState.update { it.copy(isBulkActionRunning = false, selectedTaskIds = emptySet(), isMultiSelectMode = false) }
        _uiEvent.send(UiEvent.ShowSnackbar("${ids.size} tasks deleted"))
    }

    private fun bulkReschedule(date: java.time.LocalDate) = viewModelScope.launch {
        _uiState.update { it.copy(isBulkActionRunning = true) }
        val ids = _uiState.value.selectedTaskIds.toList()
        ids.forEach { id -> updateTask(id, dueDate = date) }
        _uiState.update { it.copy(isBulkActionRunning = false, selectedTaskIds = emptySet(), isMultiSelectMode = false) }
        _uiEvent.send(UiEvent.ShowSnackbar("${ids.size} tasks rescheduled"))
    }

    private fun bulkSetPriority(priority: Priority) = viewModelScope.launch {
        _uiState.update { it.copy(isBulkActionRunning = true) }
        val ids = _uiState.value.selectedTaskIds.toList()
        ids.forEach { id -> updateTask(id, priority = priority) }
        _uiState.update { it.copy(isBulkActionRunning = false, selectedTaskIds = emptySet(), isMultiSelectMode = false) }
        _uiEvent.send(UiEvent.ShowSnackbar("${ids.size} tasks updated"))
    }

    private fun bulkAddTag(tag: String) = viewModelScope.launch {
        _uiState.update { it.copy(isBulkActionRunning = true) }
        // TODO: implement tag update in repository when available
        _uiState.update { it.copy(isBulkActionRunning = false, selectedTaskIds = emptySet(), isMultiSelectMode = false) }
        _uiEvent.send(UiEvent.ShowSnackbar("Tag added to tasks"))
    }

    private fun reapplyFilter() {
        _uiState.update { state ->
            val filtered = state.tasks
                .filterForTab(state.activeTab)
                .applyFilter(state.taskFilter)
            state.copy(filteredTasks = filtered)
        }
    }

    private fun loadTasks() {
        // Paged tasks
        getPagedTasks()
            .cachedIn(viewModelScope)
            .onEach { _pagedTasks.value = it }
            .launchIn(viewModelScope)

        observeTasks()
            .onEach { tasks ->
                val tags = tasks.flatMap { it.tags }.distinct().sorted()
                _uiState.update { current ->
                    val filtered = tasks
                        .filterForTab(current.activeTab)
                        .applyFilter(current.taskFilter)
                    current.copy(
                        isLoading = false,
                        tasks = tasks,
                        filteredTasks = filtered,
                        availableTags = tags,
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
