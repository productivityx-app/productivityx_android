package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.RecurrenceType
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.components.MinuteStepper
import com.oussama_chatri.productivityx.features.tasks.presentation.components.PriorityChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TagChip
import com.oussama_chatri.productivityx.features.tasks.presentation.components.TaskSettingRow
import com.oussama_chatri.productivityx.features.tasks.presentation.components.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.components.priorityAccentColor
import com.oussama_chatri.productivityx.features.tasks.presentation.event.AddEditTaskEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.state.AddEditTaskUiState
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.AddEditTaskViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskSheet(
    onDismiss: () -> Unit,
    taskId: String? = null,
    parentTaskId: String? = null,
    onSnackbar: (String) -> Unit = {},
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> onDismiss()
                is UiEvent.ShowSnackbar -> onSnackbar(event.message)
                else -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A24),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF252533))
            )
        }
    ) {
        AddEditTaskContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditTaskContent(
    uiState: AddEditTaskUiState,
    onEvent: (AddEditTaskEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPriorityExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRecurrenceExpanded by remember { mutableStateOf(false) }
    var showTagsExpanded by remember { mutableStateOf(false) }

    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (!uiState.isEditMode) titleFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 16.dp)
    ) {
        // Title field
        BasicTextField(
            value = uiState.title,
            onValueChange = { onEvent(AddEditTaskEvent.TitleChanged(it)) },
            textStyle = TextStyle(
                color = Color(0xFFEEEEF5),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(Color(0xFF6366F1)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (uiState.title.isEmpty()) {
                        Text(
                            "Task title\u2026",
                            color = Color(0xFF888899),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (uiState.titleError != null) {
            Text(
                text = uiState.titleError,
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Description field
        BasicTextField(
            value = uiState.description,
            onValueChange = { onEvent(AddEditTaskEvent.DescriptionChanged(it)) },
            textStyle = TextStyle(color = Color(0xFFCCCCD8), fontSize = 15.sp),
            cursorBrush = SolidColor(Color(0xFF6366F1)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (uiState.description.isEmpty()) {
                        Text("Add description\u2026", color = Color(0xFF888899), fontSize = 15.sp)
                    }
                    innerTextField()
                }
            }
        )

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Priority row with visual matrix
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.Flag, null, tint = priorityAccentColor(uiState.priority), modifier = Modifier.size(20.dp)) },
            label = "Priority",
            onClick = { showPriorityExpanded = !showPriorityExpanded },
            trailing = {
                PriorityChip(priority = uiState.priority)
            }
        )

        AnimatedVisibility(
            visible = showPriorityExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Priority.LOW to Color(0xFF6B7280),
                    Priority.MEDIUM to Color(0xFF3B82F6),
                    Priority.HIGH to Color(0xFFF59E0B),
                    Priority.URGENT to Color(0xFFEF4444)
                ).forEach { (priority, color) ->
                    val selected = uiState.priority == priority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                onEvent(AddEditTaskEvent.PriorityChanged(priority))
                                showPriorityExpanded = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = priority.displayLabel,
                            color = if (selected) color else Color(0xFF888899),
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Tags section
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.Label, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
            label = "Tags",
            onClick = { showTagsExpanded = !showTagsExpanded },
            trailing = {
                if (uiState.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.tags.take(3).forEach { tag ->
                            TagChip(tag = tag)
                        }
                        if (uiState.tags.size > 3) {
                            Text("+${uiState.tags.size - 3}", color = Color(0xFF888899), fontSize = 11.sp)
                        }
                    }
                } else {
                    Text("Add tags", color = Color(0xFF888899), fontSize = 14.sp)
                }
            }
        )

        AnimatedVisibility(
            visible = showTagsExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF252533))
                                    .clickable { onEvent(AddEditTaskEvent.RemoveTag(tag)) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tag, color = Color(0xFFCCCCD8), fontSize = 12.sp)
                                    Icon(Icons.Outlined.Close, null, tint = Color(0xFF888899), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                // Add tag input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF252533))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = uiState.newTag,
                        onValueChange = { onEvent(AddEditTaskEvent.NewTagChanged(it)) },
                        textStyle = TextStyle(color = Color(0xFFCCCCD8), fontSize = 14.sp),
                        cursorBrush = SolidColor(Color(0xFF6366F1)),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (uiState.newTag.isNotBlank()) {
                                onEvent(AddEditTaskEvent.AddTag(uiState.newTag))
                            }
                        }),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (uiState.newTag.isEmpty()) {
                                    Text("Add tag\u2026", color = Color(0xFF888899), fontSize = 14.sp)
                                }
                                inner()
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            if (uiState.newTag.isNotBlank()) {
                                onEvent(AddEditTaskEvent.AddTag(uiState.newTag))
                            }
                        },
                        enabled = uiState.newTag.isNotBlank(),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            "Add tag",
                            tint = if (uiState.newTag.isNotBlank()) Color(0xFF6366F1) else Color(0xFF888899),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Due date row
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
            label = "Due date",
            onClick = { showDatePicker = true },
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = uiState.dueDate?.toString() ?: "Set date",
                        color = if (uiState.dueDate != null) Color(0xFFCCCCD8) else Color(0xFF888899),
                        fontSize = 14.sp
                    )
                    if (uiState.dueDate != null) {
                        IconButton(
                            onClick = { onEvent(AddEditTaskEvent.DueDateChanged(null)) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Outlined.Close, stringResource(R.string.cd_clear_input), tint = Color(0xFF888899), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        )

        // Due time row (only visible if date is set)
        AnimatedVisibility(visible = uiState.dueDate != null) {
            Column {
                Divider(color = Color(0xFF252533), thickness = 1.dp)
                TaskSettingRow(
                    icon = { Icon(Icons.Outlined.AccessTime, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
                    label = "Due time",
                    onClick = { showTimePicker = true },
                    trailing = {
                        Text(
                            text = uiState.dueTime?.let { runCatching { it.format(DateTimeFormatter.ofPattern("h:mm a")) }.getOrElse { "\u2014" } } ?: "Set time",
                            color = if (uiState.dueTime != null) Color(0xFFCCCCD8) else Color(0xFF888899),
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Reminder row
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
            label = "Reminder",
            trailing = {
                Switch(
                    checked = uiState.reminderAt != null,
                    onCheckedChange = { enabled ->
                        onEvent(AddEditTaskEvent.ReminderChanged(
                            if (enabled) uiState.dueDate?.let { d ->
                                d.atStartOfDay(ZoneId.systemDefault()).toInstant()
                            } else null
                        ))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6366F1)
                    )
                )
            }
        )

        // Reminder minutes (smart defaults)
        if (uiState.reminderAt != null) {
            Divider(color = Color(0xFF252533), thickness = 1.dp)
            TaskSettingRow(
                icon = { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
                label = "Remind before",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 15, 30, 60).forEach { mins ->
                            val isSelected = uiState.reminderMinutes == mins
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF252533))
                                    .clickable { onEvent(AddEditTaskEvent.ReminderMinutesChanged(mins)) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${mins}m",
                                    color = if (isSelected) Color.White else Color(0xFF888899),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            )
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Recurrence section
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.Repeat, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
            label = "Repeat",
            onClick = { showRecurrenceExpanded = !showRecurrenceExpanded },
            trailing = {
                Text(
                    text = if (uiState.recurrenceType != RecurrenceType.NONE) {
                        uiState.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() }
                    } else "Never",
                    color = Color(0xFFCCCCD8),
                    fontSize = 14.sp
                )
            }
        )

        AnimatedVisibility(
            visible = showRecurrenceExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recurrence type chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        RecurrenceType.NONE to "None",
                        RecurrenceType.DAILY to "Daily",
                        RecurrenceType.WEEKDAYS to "Weekdays",
                        RecurrenceType.WEEKLY to "Weekly",
                        RecurrenceType.BIWEEKLY to "Biweekly",
                        RecurrenceType.MONTHLY to "Monthly",
                        RecurrenceType.YEARLY to "Yearly"
                    ).forEach { (type, label) ->
                        val isSelected = uiState.recurrenceType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF252533))
                                .clickable { onEvent(AddEditTaskEvent.RecurrenceTypeChanged(type)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF888899),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Weekly day picker
                if (uiState.recurrenceType == RecurrenceType.WEEKLY || uiState.recurrenceType == RecurrenceType.WEEKDAYS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val days = listOf("M" to 1, "T" to 2, "W" to 3, "Th" to 4, "F" to 5, "Sa" to 6, "Su" to 7)
                        days.forEach { (label, dayNum) ->
                            val isDaySelected = uiState.recurrenceDaysOfWeek?.contains(dayNum) == true
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (isDaySelected) Color(0xFF6366F1) else Color(0xFF252533))
                                    .clickable {
                                        val currentDays = uiState.recurrenceDaysOfWeek?.toMutableList() ?: mutableListOf()
                                        if (isDaySelected) currentDays.remove(dayNum) else currentDays.add(dayNum)
                                        onEvent(AddEditTaskEvent.RecurrenceDaysOfWeekChanged(currentDays.ifEmpty { null }))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isDaySelected) Color.White else Color(0xFF888899), fontSize = 11.sp, fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Estimated time row
        TaskSettingRow(
            icon = { Icon(Icons.Outlined.Timer, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
            label = "Estimated time",
            trailing = {
                MinuteStepper(
                    value = uiState.estimatedMinutes,
                    onValueChange = { onEvent(AddEditTaskEvent.EstimatedMinutesChanged(it)) }
                )
            }
        )

        Divider(color = Color(0xFF252533), thickness = 1.dp)

        // Status row (only in edit mode)
        if (uiState.isEditMode) {
            TaskSettingRow(
                icon = { Icon(Icons.Outlined.Timer, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) },
                label = "Status",
                trailing = {
                    Text(
                        text = uiState.status.displayLabel,
                        color = Color(0xFFCCCCD8),
                        fontSize = 14.sp
                    )
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.ON_HOLD, TaskStatus.DONE).forEach { status ->
                    StatusChipCompact(
                        status = status,
                        selected = uiState.status == status,
                        onClick = { onEvent(AddEditTaskEvent.StatusChanged(status)) }
                    )
                }
            }

            Divider(color = Color(0xFF252533), thickness = 1.dp)
        }

        // Subtasks (only in edit mode)
        if (uiState.isEditMode) {
            SubtaskSection(
                subtasks = uiState.subtasks,
                newSubtaskTitle = uiState.newSubtaskTitle,
                onNewTitleChange = { onEvent(AddEditTaskEvent.NewSubtaskTitleChanged(it)) },
                onAddSubtask = { onEvent(AddEditTaskEvent.AddSubtask) },
                onRemoveSubtask = { id -> onEvent(AddEditTaskEvent.RemoveSubtask(id)) }
            )
            Divider(color = Color(0xFF252533), thickness = 1.dp)
        }

        // Bottom action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.isEditMode) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Medium)
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (uiState.canSave) Color(0xFF6366F1) else Color(0xFF252533))
                    .clickable(enabled = uiState.canSave && !uiState.isSaving) {
                        onEvent(AddEditTaskEvent.Save)
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isEditMode) "Update" else "Create",
                        color = if (uiState.canSave) Color.White else Color(0xFF888899),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate
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
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }.getOrNull()
                        if (date != null) onEvent(AddEditTaskEvent.DueDateChanged(date))
                    }
                    showDatePicker = false
                }) { Text("OK", color = Color(0xFF6366F1)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFF888899))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.dueTime?.hour ?: 9,
            initialMinute = uiState.dueTime?.minute ?: 0
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditTaskEvent.DueTimeChanged(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    ))
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFF6366F1)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color(0xFF888899))
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1A1A24),
            title = {
                Text("Delete task?", color = Color(0xFFEEEEF5), fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text("This task will be moved to trash.", color = Color(0xFF888899))
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onEvent(AddEditTaskEvent.Delete)
                }) { Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color(0xFF888899))
                }
            }
        )
    }
}

// ─── Priority Button ──────────────────────────────────────────────────────────

@Composable
private fun PriorityButton(
    label: String,
    priority: Priority,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color(0xFF888899),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── Subtask Section ──────────────────────────────────────────────────────────

@Composable
private fun SubtaskSection(
    subtasks: List<Task>,
    newSubtaskTitle: String,
    onNewTitleChange: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onRemoveSubtask: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Subtasks",
            color = Color(0xFF888899),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        subtasks.forEach { subtask ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtask.title,
                    color = Color(0xFFCCCCD8),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onRemoveSubtask(subtask.id) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.cd_remove_subtask), tint = Color(0xFF888899), modifier = Modifier.size(14.dp))
                }
            }
        }

        // Add subtask inline input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF252533))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = newSubtaskTitle,
                onValueChange = onNewTitleChange,
                textStyle = TextStyle(color = Color(0xFFCCCCD8), fontSize = 14.sp),
                cursorBrush = SolidColor(Color(0xFF6366F1)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onAddSubtask() }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (newSubtaskTitle.isEmpty()) {
                            Text("Add subtask\u2026", color = Color(0xFF888899), fontSize = 14.sp)
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
                    stringResource(R.string.cd_add),
                    tint = if (newSubtaskTitle.isNotBlank()) Color(0xFF6366F1) else Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Compact Status Chip ──────────────────────────────────────────────────────

@Composable
private fun StatusChipCompact(
    status: TaskStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) Color(0xFF6366F1) else Color(0xFF252533)
    val textColor = if (selected) Color.White else Color(0xFF888899)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.displayLabel,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
