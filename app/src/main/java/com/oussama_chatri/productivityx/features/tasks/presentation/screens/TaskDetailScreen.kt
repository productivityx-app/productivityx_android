package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.ActivityEntry
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.components.CompletionCelebration
import com.oussama_chatri.productivityx.features.tasks.presentation.components.PriorityChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.StatusChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.SubtaskRow
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TagChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TaskCheckbox
import com.oussama_chatri.productivityx.features.tasks.presentation.components.priorityAccentColor
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TaskDetailEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskDetailUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TaskDetailViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.task?.title?.take(30)?.plus(
                            if ((uiState.task?.title?.length ?: 0) > 30) "\u2026" else ""
                        ) ?: stringResource(R.string.task_label),
                        color = PxColors.OnBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, stringResource(R.string.cd_back), tint = PxColors.OnSurface)
                    }
                },
                actions = {
                    uiState.task?.let { task ->
                        if (!task.isDeleted) {
                            IconButton(onClick = { onEditTask(task.id) }) {
                                Icon(Icons.Outlined.Edit, stringResource(R.string.cd_edit), tint = PxColors.Primary)
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Outlined.Delete, stringResource(R.string.cd_delete), tint = PxColors.OnSurfaceDim)
                            }
                        } else {
                            IconButton(onClick = { viewModel.onEvent(TaskDetailEvent.RestoreTask) }) {
                                Icon(Icons.Outlined.Repeat, stringResource(R.string.cd_restore), tint = PxColors.Success)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(TaskDetailEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PxColors.Primary)
                }

                uiState.error != null -> PxEmptyState(
                    icon = Icons.Outlined.Flag,
                    title = stringResource(R.string.task_not_found),
                    subtitle = uiState.error ?: stringResource(R.string.error_generic)
                )

                uiState.task != null -> TaskDetailContent(
                    task = uiState.task!!,
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = PxColors.Surface,
            title = { Text(stringResource(R.string.task_delete_confirm), color = PxColors.OnBackground, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.task_delete_message), color = PxColors.OnSurfaceDim) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.onEvent(TaskDetailEvent.DeleteTask)
                }) { Text(stringResource(R.string.delete), color = PxColors.Error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailContent(
    task: Task,
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSubtasksSection by remember { mutableStateOf(task.hasSubtasks) }
    var newSubtaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Completion celebration
        if (uiState.showCelebration) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CompletionCelebration(isMilestone = uiState.showConfetti)
            }
        }

        // Title with inline editing
        TitleSection(
            task = task,
            isEditing = uiState.isEditingTitle,
            editingTitle = uiState.editingTitle,
            onStartEdit = { onEvent(TaskDetailEvent.StartEditTitle) },
            onCancelEdit = { onEvent(TaskDetailEvent.CancelEditTitle) },
            onTitleChanged = { onEvent(TaskDetailEvent.TitleChanged(it)) },
            onSaveTitle = { onEvent(TaskDetailEvent.SaveTitle) }
        )

        // Priority + Status row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriorityChip(priority = task.priority)
            if (task.isOverdue) OverdueBadge()
            if (task.isRecurring) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(PxColors.SurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = task.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = PxColors.OnSurfaceDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Status chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.ON_HOLD, TaskStatus.DONE)
                .forEach { status ->
                    StatusChip(
                        status = status,
                        selected = task.status == status,
                        onClick = { onEvent(TaskDetailEvent.StatusChanged(status)) },
                        modifier = Modifier.weight(1f)
                    )
                }
        }

        // Tags
        if (task.tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                task.tags.forEach { tag ->
                    TagChip(tag = tag)
                }
            }
        }

        // Description with inline editing
        DescriptionSection(
            task = task,
            isEditing = uiState.isEditingDescription,
            editingDescription = uiState.editingDescription,
            onStartEdit = { onEvent(TaskDetailEvent.StartEditDescription) },
            onCancelEdit = { onEvent(TaskDetailEvent.CancelEditDescription) },
            onDescriptionChanged = { onEvent(TaskDetailEvent.DescriptionChanged(it)) },
            onSaveDescription = { onEvent(TaskDetailEvent.SaveDescription) }
        )

        // Metadata card
        DetailCard {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                if (task.dueDate != null) {
                    DetailRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = stringResource(R.string.task_due_date),
                        value = task.dueDate.toString(),
                        valueColor = if (task.isOverdue) PxColors.Error else PxColors.OnSurface,
                        onClick = { showDatePicker = true }
                    )
                }
                if (task.dueTime != null) {
                    DetailRow(
                        icon = Icons.Outlined.AccessTime,
                        label = stringResource(R.string.task_due_time),
                        value = runCatching { task.dueTime.format(DateTimeFormatter.ofPattern("h:mm a")) }.getOrElse { "\u2014" },
                        onClick = { showTimePicker = true }
                    )
                }
                if (task.reminderAt != null) {
                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        label = stringResource(R.string.task_reminder),
                        value = stringResource(R.string.task_reminder_set)
                    )
                }
                if (task.estimatedMinutes != null || task.actualMinutes > 0) {
                    val estLabel = stringResource(R.string.task_estimated_abbrev)
                    val actualLabel = stringResource(R.string.task_actual_abbrev)
                    DetailRow(
                        icon = Icons.Outlined.Timer,
                        label = stringResource(R.string.task_time),
                        value = buildString {
                            if (task.estimatedMinutes != null) append("$estLabel ${task.estimatedMinutes}m")
                            if (task.estimatedMinutes != null && task.actualMinutes > 0) append(" \u00B7 ")
                            if (task.actualMinutes > 0) append("$actualLabel ${task.actualMinutes}m")
                        }
                    )
                }
                if (task.assigneeId != null) {
                    DetailRow(
                        icon = Icons.Outlined.Person,
                        label = stringResource(R.string.task_assignee),
                        value = task.assigneeName ?: stringResource(R.string.task_assigned)
                    )
                }
                if (task.completedAt != null) {
                    DetailRow(
                        icon = Icons.Outlined.CheckCircle,
                        label = stringResource(R.string.task_completed),
                        value = task.completedAt.toString().take(10),
                        valueColor = PxColors.Success
                    )
                }
            }
        }

        // Priority selector with visual matrix
        PriorityMatrixSection(
            currentPriority = task.priority,
            onPrioritySelected = { onEvent(TaskDetailEvent.UpdatePriority(it)) }
        )

        // Subtasks section
        SubtaskSection(
            task = task,
            newSubtaskTitle = newSubtaskTitle,
            onNewTitleChange = { newSubtaskTitle = it },
            onAddSubtask = {
                if (newSubtaskTitle.isNotBlank()) {
                    onEvent(TaskDetailEvent.AddSubtaskInline(newSubtaskTitle))
                    newSubtaskTitle = ""
                }
            },
            onToggleSubtask = { onEvent(TaskDetailEvent.ToggleSubtask(it)) },
            isExpanded = showSubtasksSection,
            onToggleExpanded = { showSubtasksSection = !showSubtasksSection }
        )

        // Activity log
        ActivityLogSection(activityLog = task.activityLog)

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = task.dueDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = runCatching {
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }.getOrNull()
                        if (date != null) onEvent(TaskDetailEvent.UpdateDueDate(date))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok), color = PxColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = task.dueTime?.hour ?: 9,
            initialMinute = task.dueTime?.minute ?: 0
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = PxColors.SurfaceVariant,
            confirmButton = {
                TextButton(onClick = {
                    onEvent(TaskDetailEvent.UpdateDueTime(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    ))
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok), color = PxColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

// ─── Title Section with inline editing ────────────────────────────────────────

@Composable
private fun TitleSection(
    task: Task,
    isEditing: Boolean,
    editingTitle: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSaveTitle: () -> Unit
) {
    if (isEditing) {
        Column {
            BasicTextField(
                value = editingTitle,
                onValueChange = onTitleChanged,
                textStyle = TextStyle(
                    color = PxColors.OnBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(PxColors.Primary),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSaveTitle() }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box { innerTextField() }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancelEdit) {
                    Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim, fontSize = 13.sp)
                }
                TextButton(onClick = onSaveTitle) {
                    Text(stringResource(R.string.save), color = PxColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    } else {
        Column {
            Text(
                text = task.title,
                color = if (task.status == TaskStatus.DONE) PxColors.OnSurfaceDim else PxColors.OnBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.clickable(onClick = onStartEdit)
            )
        }
    }
}

// ─── Description Section with inline editing ──────────────────────────────────

@Composable
private fun DescriptionSection(
    task: Task,
    isEditing: Boolean,
    editingDescription: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSaveDescription: () -> Unit
) {
    Column {
        if (isEditing) {
            BasicTextField(
                value = editingDescription,
                onValueChange = onDescriptionChanged,
                textStyle = TextStyle(
                    color = PxColors.OnSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(PxColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PxColors.Surface)
                    .padding(16.dp)
                    .height(120.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (editingDescription.isEmpty()) {
                            Text(stringResource(R.string.task_add_description), color = PxColors.OnSurfaceDim, fontSize = 15.sp)
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancelEdit) {
                    Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim, fontSize = 13.sp)
                }
                TextButton(onClick = onSaveDescription) {
                    Text(stringResource(R.string.save), color = PxColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else if (!task.description.isNullOrBlank()) {
            DetailCard(
                modifier = Modifier.clickable(onClick = onStartEdit)
            ) {
                Text(
                    text = task.description,
                    color = PxColors.OnSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PxColors.Surface)
                    .clickable(onClick = onStartEdit)
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Edit, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.task_add_description), color = PxColors.OnSurfaceDim, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── Priority Matrix Section ──────────────────────────────────────────────────

@Composable
private fun PriorityMatrixSection(
    currentPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        DetailCard(
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Flag, null, tint = priorityAccentColor(currentPriority), modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.task_priority), color = PxColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text(currentPriority.name.lowercase().replaceFirstChar { it.uppercase() }, color = priorityAccentColor(currentPriority), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        AnimatedVisibility(visible = expanded) {
            // Visual priority matrix (2x2: Urgent/Important quadrant)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PxColors.Surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.priority_low_importance), color = PxColors.OnSurfaceDim, fontSize = 10.sp)
                    Text(stringResource(R.string.priority_high_importance), color = PxColors.OnSurfaceDim, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Row 1: Urgent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.priority_urgent), color = PxColors.OnSurfaceDim, fontSize = 10.sp, modifier = Modifier.width(40.dp))
                    PriorityQuadrant(
                        label = stringResource(R.string.priority_urgent_low),
                        priority = Priority.HIGH,
                        isSelected = currentPriority == Priority.HIGH,
                        color = Color(0xFFF59E0B),
                        onClick = { onPrioritySelected(Priority.HIGH) },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityQuadrant(
                        label = stringResource(R.string.priority_urgent_high),
                        priority = Priority.URGENT,
                        isSelected = currentPriority == Priority.URGENT,
                        color = Color(0xFFEF4444),
                        onClick = { onPrioritySelected(Priority.URGENT) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Not Urgent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.priority_not_urgent), color = PxColors.OnSurfaceDim, fontSize = 10.sp, modifier = Modifier.width(40.dp))
                    PriorityQuadrant(
                        label = stringResource(R.string.priority_not_urgent_low),
                        priority = Priority.LOW,
                        isSelected = currentPriority == Priority.LOW,
                        color = Color(0xFF6B7280),
                        onClick = { onPrioritySelected(Priority.LOW) },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityQuadrant(
                        label = stringResource(R.string.priority_not_urgent_high),
                        priority = Priority.MEDIUM,
                        isSelected = currentPriority == Priority.MEDIUM,
                        color = Color(0xFF3B82F6),
                        onClick = { onPrioritySelected(Priority.MEDIUM) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Color-coded buttons row
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (p == currentPriority) priorityAccentColor(p)
                                    else priorityAccentColor(p).copy(alpha = 0.15f)
                                )
                                .clickable { onPrioritySelected(p) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (p == currentPriority) Color.White else priorityAccentColor(p),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityQuadrant(
    label: String,
    priority: Priority,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            color = if (isSelected) color else PxColors.OnSurfaceDim,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── Subtask Section ──────────────────────────────────────────────────────────

@Composable
private fun SubtaskSection(
    task: Task,
    newSubtaskTitle: String,
    onNewTitleChange: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    DetailCard(
        modifier = Modifier.clickable(onClick = onToggleExpanded)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.task_subtasks), color = PxColors.OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (task.hasSubtasks) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(PxColors.Primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${task.completedSubtaskCount}/${task.subtaskCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Subtask progress ring
                if (task.hasSubtasks) {
                    val progress = task.subtaskProgress
                    Box(modifier = Modifier.size(20.dp)) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(
                                color = PxColors.SurfaceVariant,
                                radius = size.minDimension / 2
                            )
                            drawArc(
                                color = PxColors.Primary,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    if (task.hasSubtasks) {
                        task.subtasks.forEach { subtask ->
                            SubtaskRow(
                                task = subtask,
                                onCheckChange = { id, _ -> onToggleSubtask(id) }
                            )
                        }
                    }

                    // Add subtask inline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PxColors.SurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BasicTextField(
                            value = newSubtaskTitle,
                            onValueChange = onNewTitleChange,
                            textStyle = TextStyle(color = PxColors.OnSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(PxColors.Primary),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onAddSubtask() }),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                Box {
                                    if (newSubtaskTitle.isEmpty()) {
                                        Text(stringResource(R.string.task_add_subtask), color = PxColors.OnSurfaceDim, fontSize = 14.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                        IconButton(
                            onClick = onAddSubtask,
                            enabled = newSubtaskTitle.isNotBlank(),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                stringResource(R.string.task_add_subtask),
                                tint = if (newSubtaskTitle.isNotBlank()) PxColors.Primary else PxColors.OnSurfaceDim,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Activity Log Section ─────────────────────────────────────────────────────

@Composable
private fun ActivityLogSection(
    activityLog: List<ActivityEntry>
) {
    if (activityLog.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    DetailCard(
        modifier = Modifier.clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.History, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.task_activity), color = PxColors.OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = if (expanded) stringResource(R.string.task_hide) else stringResource(R.string.task_events, activityLog.size),
                    color = PxColors.Primary,
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activityLog.take(20).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .offset(y = 6.dp)
                                    .clip(CircleShape)
                                    .background(PxColors.OnSurfaceDim)
                            )
                            Column {
                                Text(
                                    text = entry.action,
                                    color = PxColors.OnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (entry.details != null) {
                                    Text(
                                        text = entry.details,
                                        color = PxColors.OnSurfaceDim,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = entry.timestamp.toString().take(19).replace("T", " "),
                                    color = PxColors.OnSurfaceDim.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Helper composables ───────────────────────────────────────────────────────

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
    ) {
        content()
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = PxColors.OnSurface,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
        Text(label, color = PxColors.OnSurfaceDim, fontSize = 14.sp, modifier = Modifier.width(80.dp))
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OverdueBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(PxColors.Error.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            stringResource(R.string.task_overdue),
            color = PxColors.Error,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
