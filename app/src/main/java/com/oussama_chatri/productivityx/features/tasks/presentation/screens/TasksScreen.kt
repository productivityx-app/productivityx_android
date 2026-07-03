package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.core.ui.components.PxLoadingOverlay
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.model.TaskSmartFilter
import com.oussama_chatri.productivityx.features.tasks.presentation.components.CalendarDayCell
import com.oussama_chatri.productivityx.features.tasks.presentation.components.KanbanColumnHeader
import com.oussama_chatri.productivityx.features.tasks.presentation.components.KanbanTaskCard
import com.oussama_chatri.productivityx.features.tasks.presentation.components.SmartFilterChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TaskEmptyState
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TaskListItem
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TimelineTaskBar
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TasksEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TasksUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.state.label
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TasksViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit,
    onAddTask: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showReschedulePicker by remember { mutableStateOf(false) }
    var showPriorityPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                }
                else -> Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = { viewModel.onEvent(TasksEvent.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // View mode toggle + Filter
                TaskViewToggle(
                    viewMode = uiState.viewMode,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    selectedCount = uiState.selectedTaskIds.size,
                    onToggleView = { viewModel.onEvent(TasksEvent.ToggleView(it)) },
                    onExitMultiSelect = { viewModel.onEvent(TasksEvent.ExitMultiSelectMode) },
                    onBulkComplete = { viewModel.onEvent(TasksEvent.BulkComplete) },
                    onBulkDelete = { viewModel.onEvent(TasksEvent.BulkDelete) },
                    onSelectAll = { viewModel.onEvent(TasksEvent.SelectAll) },
                    onNavigateToStats = onNavigateToStats
                )

                // Search bar
                SearchBar(
                    query = uiState.taskFilter.searchQuery,
                    onQueryChange = { viewModel.onEvent(TasksEvent.SetSearchQuery(it)) }
                )

                // Smart filters row
                SmartFilterRow(
                    activeFilter = uiState.taskFilter.smartFilter,
                    tasks = uiState.tasks,
                    onFilterSelected = { viewModel.onEvent(TasksEvent.SetSmartFilter(it)) }
                )

                // Main content area based on view mode
                AnimatedContent(
                    targetState = uiState.viewMode,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "viewModeTransition"
                ) { mode ->
                    when (mode) {
                        TaskView.LIST -> TaskListView(
                            tasks = uiState.filteredTasks,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            selectedTaskIds = uiState.selectedTaskIds,
                            onTaskClick = onTaskClick,
                            onComplete = { id -> viewModel.onEvent(TasksEvent.CompleteTask(id)) },
                            onDelete = { id -> viewModel.onEvent(TasksEvent.DeleteTask(id)) },
                            onToggleSelect = { viewModel.onEvent(TasksEvent.ToggleTaskSelection(it)) },
                            onAddTask = onAddTask
                        )
                        TaskView.KANBAN -> KanbanView(
                            tasks = uiState.filteredTasks,
                            onTaskClick = onTaskClick,
                            onStatusChange = { id, status ->
                                viewModel.onEvent(TasksEvent.MoveTaskToStatus(id, status))
                            },
                            onAddTask = onAddTask
                        )
                        TaskView.CALENDAR -> CalendarView(
                            tasks = uiState.tasks,
                            startDate = uiState.calendarStartDate,
                            selectedDate = uiState.calendarSelectedDate,
                            onNavigateMonth = { viewModel.onEvent(TasksEvent.CalendarNavigateMonth(it)) },
                            onSelectDate = { viewModel.onEvent(TasksEvent.SelectCalendarDate(it)) },
                            onTaskClick = onTaskClick
                        )
                        TaskView.TIMELINE -> TimelineView(
                            tasks = uiState.filteredTasks.filter { it.dueDate != null },
                            startDate = uiState.timelineStartDate,
                            endDate = uiState.timelineEndDate,
                            onTaskClick = onTaskClick,
                            onZoomIn = { viewModel.onEvent(TasksEvent.TimelineZoomIn()) },
                            onZoomOut = { viewModel.onEvent(TasksEvent.TimelineZoomOut()) },
                            onPan = { viewModel.onEvent(TasksEvent.TimelinePan(it)) }
                        )
                    }
                }
            }
        }

        // Bulk action bar at bottom
        AnimatedVisibility(
            visible = uiState.isMultiSelectMode,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BulkActionBar(
                selectedCount = uiState.selectedTaskIds.size,
                onComplete = { viewModel.onEvent(TasksEvent.BulkComplete) },
                onDelete = { viewModel.onEvent(TasksEvent.BulkDelete) },
                onReschedule = { showReschedulePicker = true },
                onSetPriority = { showPriorityPicker = true },
                onClearSelection = { viewModel.onEvent(TasksEvent.ClearSelection) }
            )
        }

        if (showReschedulePicker) {
            DatePickerDialog(
                onDismissRequest = { showReschedulePicker = false },
                confirmButton = {
                    TextButton(onClick = { showReschedulePicker = false }) { Text("OK") }
                }
            ) {
                val datePickerState = rememberDatePickerState()
                DatePicker(state = datePickerState)
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onEvent(TasksEvent.BulkReschedule(date))
                    }
                }
            }
        }

        if (showPriorityPicker) {
            AlertDialog(
                onDismissRequest = { showPriorityPicker = false },
                title = { Text("Set Priority") },
                text = {
                    Column {
                        Priority.entries.forEach { priority ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(TasksEvent.BulkSetPriority(priority))
                                        showPriorityPicker = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = false,
                                    onClick = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(priority.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPriorityPicker = false }) { Text("Cancel") }
                },
                containerColor = PxColors.Surface
            )
        }

        if (uiState.isLoading) PxLoadingOverlay()
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── View toggle bar ──────────────────────────────────────────────────────────

@Composable
private fun TaskViewToggle(
    viewMode: TaskView,
    isMultiSelectMode: Boolean,
    selectedCount: Int,
    onToggleView: (TaskView) -> Unit,
    onExitMultiSelect: () -> Unit,
    onBulkComplete: () -> Unit,
    onBulkDelete: () -> Unit,
    onSelectAll: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(targetState = isMultiSelectMode, label = "multiSelectToggle") { multiSelect ->
            if (multiSelect) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onExitMultiSelect) {
                        Icon(Icons.Filled.Close, "Exit multi-select", tint = PxColors.OnSurface)
                    }
                    Text(
                        "$selectedCount selected",
                        color = PxColors.OnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Outlined.SelectAll, "Select all", tint = PxColors.Primary)
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (viewMode) {
                            TaskView.LIST -> "List"
                            TaskView.KANBAN -> "Board"
                            TaskView.CALENDAR -> "Calendar"
                            TaskView.TIMELINE -> "Timeline"
                        },
                        color = PxColors.OnBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            // Stats button
            IconButton(onClick = onNavigateToStats) {
                Icon(Icons.Outlined.BarChart, "Statistics", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(20.dp))
            }

            // View mode switcher
            val views = listOf(TaskView.LIST, TaskView.KANBAN, TaskView.CALENDAR, TaskView.TIMELINE)
            views.forEach { v ->
                val isActive = v == viewMode
                IconButton(
                    onClick = { onToggleView(v) }
                ) {
                    Icon(
                        imageVector = when (v) {
                            TaskView.LIST -> Icons.Outlined.ViewList
                            TaskView.KANBAN -> Icons.Outlined.ViewKanban
                            TaskView.CALENDAR -> Icons.Outlined.CalendarMonth
                            TaskView.TIMELINE -> Icons.Outlined.Timeline
                        },
                        contentDescription = v.name,
                        tint = if (isActive) PxColors.Primary else PxColors.OnSurfaceDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF252533))
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = "Search",
                tint = Color(0xFF888899),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (query.isBlank()) "Search or filter tasks\u2026" else query,
                color = if (query.isBlank()) Color(0xFF888899) else Color(0xFFCCCCD8),
                fontSize = 14.sp
            )
        }
    }
}

// ─── Smart Filter Row ─────────────────────────────────────────────────────────

@Composable
private fun SmartFilterRow(
    activeFilter: TaskSmartFilter,
    tasks: List<Task>,
    onFilterSelected: (TaskSmartFilter) -> Unit
) {
    val filters = remember {
        listOf(
            TaskSmartFilter.ALL to "All",
            TaskSmartFilter.TODAY to "Today",
            TaskSmartFilter.UPCOMING to "Upcoming",
            TaskSmartFilter.OVERDUE to "Overdue",
            TaskSmartFilter.NO_DATE to "No Date",
            TaskSmartFilter.COMPLETED to "Done"
        )
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (filter, label) ->
            val count by remember(filter, tasks) {
                derivedStateOf {
                    when (filter) {
                        TaskSmartFilter.ALL -> tasks.size
                        TaskSmartFilter.TODAY -> tasks.count { it.isDueToday }
                        TaskSmartFilter.UPCOMING -> tasks.count { it.dueDate != null && it.dueDate.isAfter(LocalDate.now()) && it.status != TaskStatus.DONE }
                        TaskSmartFilter.OVERDUE -> tasks.count { it.isOverdue }
                        TaskSmartFilter.NO_DATE -> tasks.count { it.dueDate == null && it.status != TaskStatus.DONE && it.status != TaskStatus.CANCELLED }
                        TaskSmartFilter.COMPLETED -> tasks.count { it.status == TaskStatus.DONE }
                    }
                }
            }
            SmartFilterChip(
                label = label,
                isSelected = activeFilter == filter,
                count = count,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ─── List View ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListView(
    tasks: List<Task>,
    isMultiSelectMode: Boolean,
    selectedTaskIds: Set<String>,
    onTaskClick: (String) -> Unit,
    onComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onAddTask: () -> Unit
) {
    if (tasks.isEmpty()) {
        TaskEmptyState(viewMode = TaskView.LIST, onAddTask = onAddTask)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tasks, key = { it.id }) { task ->
            if (isMultiSelectMode) {
                // Selection mode: click to toggle
                TaskListItem(
                    task = task,
                    onTaskClick = { onToggleSelect(task.id) },
                    onCheckChange = { id, _ -> onToggleSelect(id) },
                    isSelected = selectedTaskIds.contains(task.id)
                )
            } else {
                SwipeableTaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onComplete = { onComplete(task.id) },
                    onDelete = { onDelete(task.id) },
                    onLongClick = { onToggleSelect(task.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onComplete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * 0.3f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isCompleteSwipe = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val isDeleteSwipe = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCompleteSwipe -> PxColors.Success
                            isDeleteSwipe -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = when {
                    isCompleteSwipe -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                Icon(
                    imageVector = if (isCompleteSwipe) Icons.Outlined.CheckCircle else Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PxColors.Surface)
        ) {
            TaskListItem(
                task = task,
                onTaskClick = { onTaskClick() },
                onCheckChange = { _, _ -> onComplete() }
            )
        }
    }
}

// ─── Kanban View ──────────────────────────────────────────────────────────────

@Composable
private fun KanbanView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onStatusChange: (String, TaskStatus) -> Unit,
    onAddTask: () -> Unit
) {
    val kanbanColumns = listOf(
        TaskStatus.TODO,
        TaskStatus.IN_PROGRESS,
        TaskStatus.ON_HOLD,
        TaskStatus.DONE
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(kanbanColumns) { status ->
            val columnTasks = tasks.filter { it.status == status }
            KanbanColumn(
                status = status,
                tasks = columnTasks,
                onTaskClick = onTaskClick,
                onStatusChange = onStatusChange,
                onAddTask = onAddTask
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    status: TaskStatus,
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onStatusChange: (String, TaskStatus) -> Unit,
    onAddTask: () -> Unit
) {
    var dropActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KanbanColumnHeader(status = status, count = tasks.size)

        // Drop zone at top
        androidx.compose.animation.AnimatedVisibility(visible = dropActive) {
            com.oussama_chatri.productivityx.features.tasks.presentation.components.DropZone(
                isActive = dropActive,
                label = "Drop here"
            )
        }

        if (tasks.isEmpty() && !dropActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Drop tasks here",
                    color = PxColors.OnSurfaceDim,
                    fontSize = 13.sp
                )
            }
        }

        tasks.forEach { task ->
            KanbanTaskCard(
                task = task,
                onClick = { onTaskClick(task.id) },
                onDragStart = { dropActive = true }
            )
        }

        // Add task button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAddTask)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = PxColors.Primary, modifier = Modifier.size(16.dp))
                Text("Add task", color = PxColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Calendar View ────────────────────────────────────────────────────────────

@Composable
private fun CalendarView(
    tasks: List<Task>,
    startDate: LocalDate,
    selectedDate: LocalDate?,
    onNavigateMonth: (Int) -> Unit,
    onSelectDate: (LocalDate?) -> Unit,
    onTaskClick: (String) -> Unit
) {
    val yearMonth = YearMonth.from(startDate)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sun
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Month header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                color = PxColors.OnBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onNavigateMonth(-1) }) {
                    Icon(Icons.Filled.Close, "Previous", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
                }
                // Use a simple text-based nav instead
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onNavigateMonth(-1) }
                        .padding(8.dp)
                ) {
                    Text("\u25C0", color = PxColors.Primary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onNavigateMonth(1) }
                        .padding(8.dp)
                ) {
                    Text("\u25B6", color = PxColors.Primary, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    color = PxColors.OnSurfaceDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - firstDayOfWeek + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val dayTasks = tasks.filter { it.dueDate == date }
                        CalendarDayCell(
                            day = dayIndex,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            taskCount = dayTasks.size,
                            hasTasks = dayTasks.isNotEmpty(),
                            onClick = {
                                if (selectedDate == date) {
                                    onSelectDate(null)
                                } else {
                                    onSelectDate(date)
                                }
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks for selected date
        val dateToShow = selectedDate ?: today
        val dayTasks = tasks.filter { it.dueDate == dateToShow }

        Text(
            text = "Tasks for ${dateToShow.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
            color = PxColors.OnBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (dayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tasks on this day",
                    color = PxColors.OnSurfaceDim,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(dayTasks) { task ->
                    TaskListItem(
                        task = task,
                        onTaskClick = { onTaskClick(task.id) },
                        onCheckChange = { _, _ -> }
                    )
                }
            }
        }
    }
}

// ─── Timeline View (Gantt-style) ──────────────────────────────────────────────

@Composable
private fun TimelineView(
    tasks: List<Task>,
    startDate: LocalDate,
    endDate: LocalDate,
    onTaskClick: (String) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onPan: (Int) -> Unit
) {
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(1)
    val today = LocalDate.now()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPan(-7) }
                        .padding(6.dp)
                ) {
                    Text("\u25C0", color = PxColors.Primary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPan(7) }
                        .padding(6.dp)
                ) {
                    Text("\u25B6", color = PxColors.Primary, fontSize = 14.sp)
                }
            }

            Text(
                text = "${startDate.format(DateTimeFormatter.ofPattern("MMM d"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                color = PxColors.OnSurface,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onZoomIn)
                        .padding(6.dp)
                ) {
                    Text("+", color = PxColors.Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onZoomOut)
                        .padding(6.dp)
                ) {
                    Text("\u2212", color = PxColors.Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            TaskEmptyState(viewMode = TaskView.TIMELINE, onAddTask = {})
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Calendar header row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    for (i in 0 until totalDays.coerceAtMost(31)) {
                        val date = startDate.plusDays(i.toLong())
                        val isTodayCol = date == today
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .background(if (isTodayCol) PxColors.Primary.copy(alpha = 0.15f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isTodayCol) PxColors.Primary else PxColors.OnSurfaceDim,
                                fontSize = 10.sp,
                                fontWeight = if (isTodayCol) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Tasks
            items(tasks) { task ->
                val taskStart = task.dueDate ?: return@items
                val offset = ChronoUnit.DAYS.between(startDate, taskStart).toInt().coerceAtLeast(0)
                val duration = task.estimatedMinutes?.let { (it / 60).coerceAtLeast(1) } ?: 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTaskClick(task.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Task label
                    Text(
                        text = task.title,
                        color = PxColors.OnSurface,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(80.dp)
                    )

                    // Timeline bar
                    Box(
                        modifier = Modifier
                            .offset(x = (offset * 28).dp)
                            .width((duration * 28).dp.coerceAtLeast(20.dp))
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (task.priority) {
                                    Priority.LOW -> Color(0xFF6B7280)
                                    Priority.MEDIUM -> Color(0xFF3B82F6)
                                    Priority.HIGH -> Color(0xFFF59E0B)
                                    Priority.URGENT -> Color(0xFFEF4444)
                                }.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = task.title,
                            color = PxColors.OnBackground,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Bulk Action Bar ──────────────────────────────────────────────────────────

@Composable
private fun BulkActionBar(
    selectedCount: Int,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onReschedule: () -> Unit,
    onSetPriority: () -> Unit,
    onClearSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PxColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BulkActionButton(
            icon = Icons.Outlined.CheckCircle,
            label = "Complete",
            onClick = onComplete
        )
        BulkActionButton(
            icon = Icons.Outlined.Delete,
            label = "Delete",
            onClick = onDelete
        )
        BulkActionButton(
            icon = Icons.Outlined.Schedule,
            label = "Reschedule",
            onClick = onReschedule
        )
        BulkActionButton(
            icon = Icons.Outlined.Edit,
            label = "Priority",
            onClick = onSetPriority
        )
    }
}

@Composable
private fun BulkActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = PxColors.Primary, modifier = Modifier.size(20.dp))
        Text(label, color = PxColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
