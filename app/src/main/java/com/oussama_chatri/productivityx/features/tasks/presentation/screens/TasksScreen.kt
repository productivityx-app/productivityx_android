package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.components.PxLoadingOverlay
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.components.KanbanColumnHeader
import com.oussama_chatri.productivityx.features.tasks.presentation.components.KanbanTaskCard
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TaskListItem
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TasksEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskTab
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TasksUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.state.label
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (String) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                    // handle snackbar action if provided
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TasksTopBar(
                viewMode = uiState.viewMode,
                onToggleView = { viewModel.onEvent(TasksEvent.ToggleView(it)) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = {
                    Text("New Task", fontWeight = FontWeight.SemiBold)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TaskTabRow(
                activeTab = uiState.activeTab,
                onTabSelected = { viewModel.onEvent(TasksEvent.SelectTab(it)) }
            )

            AnimatedContent(
                targetState = uiState.viewMode,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "viewModeTransition"
            ) { mode ->
                when (mode) {
                    TaskView.LIST -> TaskListView(
                        tasks = uiState.tasks,
                        onTaskClick = onTaskClick,
                        onComplete = { id -> viewModel.onEvent(TasksEvent.CompleteTask(id)) },
                        onDelete = { id -> viewModel.onEvent(TasksEvent.DeleteTask(id)) }
                    )
                    TaskView.KANBAN -> KanbanView(
                        tasks = uiState.tasks,
                        onTaskClick = onTaskClick,
                        onStatusChange = { id, status ->
                            // Status chip in kanban column changes status
                        },
                        onAddTask = onAddTask
                    )
                }
            }
        }

        if (uiState.isLoading) PxLoadingOverlay()
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksTopBar(
    viewMode: TaskView,
    onToggleView: (TaskView) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Tasks",
                color = Color(0xFFEEEEF5),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = Color(0xFF888899))
            }
            IconButton(onClick = {
                onToggleView(if (viewMode == TaskView.LIST) TaskView.KANBAN else TaskView.LIST)
            }) {
                Icon(
                    imageVector = if (viewMode == TaskView.LIST) Icons.Outlined.ViewKanban else Icons.Outlined.ViewList,
                    contentDescription = "Toggle view",
                    tint = Color(0xFF6366F1)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

// ─── Tab Row ──────────────────────────────────────────────────────────────────

@Composable
private fun TaskTabRow(
    activeTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(TaskTab.entries) { tab ->
            val isActive = tab == activeTab
            val bgColor by animateColorAsState(
                targetValue = if (isActive) Color(0xFF6366F1) else Color(0xFF252533),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else Color(0xFF888899),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tabText"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ─── List View ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onComplete: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (tasks.isEmpty()) {
        PxEmptyState(
            icon = Icons.Outlined.CheckCircle,
            title = "No tasks here",
            subtitle = "Create your first task to get started."
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tasks, key = { it.id }) { task ->
            SwipeableTaskItem(
                task = task,
                onTaskClick = { onTaskClick(task.id) },
                onComplete = { onComplete(task.id) },
                onDelete = { onDelete(task.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
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
                    false // don't dismiss — just trigger action
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
                            isCompleteSwipe -> Color(0xFF22C55E)
                            isDeleteSwipe -> Color(0xFFEF4444)
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
                .background(Color(0xFF1A1A24))
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
    onAddTask: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A24))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KanbanColumnHeader(status = status, count = tasks.size)

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tasks here",
                    color = Color(0xFF888899),
                    fontSize = 13.sp
                )
            }
        }

        tasks.forEach { task ->
            KanbanTaskCard(
                task = task,
                onClick = { onTaskClick(task.id) }
            )
        }

        // Add task button at bottom of column
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
                Icon(Icons.Outlined.Add, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                Text("Add task", color = Color(0xFF6366F1), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
