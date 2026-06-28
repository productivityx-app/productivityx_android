package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.components.PriorityChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.StatusChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.SubtaskRow
import com.oussama_chatri.productivityx.features.tasks.presentation.components.priorityAccentColor
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TaskDetailEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TaskDetailViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onEditTask: (String) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.task?.title?.take(30)?.plus(
                            if ((uiState.task?.title?.length ?: 0) > 30) "…" else ""
                        ) ?: "Task",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    uiState.task?.let { task ->
                        if (!task.isDeleted) {
                            IconButton(onClick = { onEditTask(task.id) }) {
                                Icon(Icons.Outlined.Edit, null, tint = PxColors.Primary)
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            IconButton(onClick = { viewModel.onEvent(TaskDetailEvent.RestoreTask) }) {
                                Icon(Icons.Outlined.Refresh, null, tint = PxColors.Success)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PxColors.Primary)
            }

            uiState.error != null -> PxEmptyState(
                icon = Icons.Outlined.Flag,
                title = "Task not found",
                subtitle = uiState.error ?: "Something went wrong"
            )

            uiState.task != null -> TaskDetailContent(
                task = uiState.task!!,
                onStatusChange = { status -> viewModel.onEvent(TaskDetailEvent.StatusChanged(status)) },
                onSubtaskCheck = { subtaskId, done ->
                    viewModel.onEvent(TaskDetailEvent.StatusChanged(
                        if (done) TaskStatus.DONE else TaskStatus.TODO
                    ))
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete task?", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold) },
            text = { Text("This task will be moved to trash.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.onEvent(TaskDetailEvent.DeleteTask)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onSubtaskCheck: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title + completion decoration
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = task.title,
                color = if (task.status == TaskStatus.DONE) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else TextDecoration.None
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(priority = task.priority)
                if (task.isOverdue) {
                    OverdueBadge()
                }
            }
        }

        // Status chips row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.ON_HOLD, TaskStatus.DONE)
                .forEach { status ->
                    StatusChip(
                        status = status,
                        selected = task.status == status,
                        onClick = { onStatusChange(status) },
                        modifier = Modifier.weight(1f)
                    )
                }
        }

        // Description
        if (!task.description.isNullOrBlank()) {
            DetailCard {
                Text(
                    text = task.description,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // Metadata rows
        DetailCard {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                if (task.dueDate != null) {
                    DetailRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Due date",
                        value = task.dueDate.toString(),
                        valueColor = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (task.dueTime != null) {
                    DetailRow(
                        icon = Icons.Outlined.AccessTime,
                        label = "Due time",
                        value = task.dueTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                    )
                }
                if (task.reminderAt != null) {
                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        label = "Reminder",
                        value = "Set"
                    )
                }
                if (task.estimatedMinutes != null || task.actualMinutes > 0) {
                    DetailRow(
                        icon = Icons.Outlined.Timer,
                        label = "Time",
                        value = buildString {
                            if (task.estimatedMinutes != null) append("Est. ${task.estimatedMinutes}m")
                            if (task.estimatedMinutes != null && task.actualMinutes > 0) append(" · ")
                            if (task.actualMinutes > 0) append("Actual ${task.actualMinutes}m")
                        }
                    )
                }
                if (task.completedAt != null) {
                    DetailRow(
                        icon = Icons.Outlined.CheckCircle,
                        label = "Completed",
                        value = task.completedAt.toString().take(10),
                        valueColor = PxColors.Success
                    )
                }
            }
        }

        // Subtasks section
        if (task.hasSubtasks) {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Subtasks",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${task.completedSubtaskCount}/${task.subtaskCount}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    task.subtasks.forEach { subtask ->
                        SubtaskRow(
                            task = subtask,
                            onCheckChange = { id, done -> onSubtaskCheck(id, done) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.width(80.dp))
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OverdueBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            "Overdue",
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
