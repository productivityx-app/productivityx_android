package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.AddEditEventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d").withZone(ZoneId.systemDefault())
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventSheet(
    eventId: String?,
    prefilledStartMs: Long?,
    onDismiss: () -> Unit,
    viewModel: AddEditEventViewModel = hiltViewModel()
) {
    val state      by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(eventId, prefilledStartMs) {
        viewModel.init(eventId, prefilledStartMs)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> onDismiss()
                else                   -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF1A1A24),
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF252533))
            )
        }
    ) {
        AddEditEventSheetContent(
            state     = state,
            onEvent   = viewModel::onEvent,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditEventSheetContent(
    state: com.oussama_chatri.productivityx.features.events.presentation.state.AddEditEventUiState,
    onEvent: (AddEditEventUiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var showRecurrencePicker by remember { mutableStateOf(false) }
    var showReminderPicker   by remember { mutableStateOf(false) }
    var showStartDatePicker  by remember { mutableStateOf(false) }
    var showStartTimePicker  by remember { mutableStateOf(false) }
    var showEndDatePicker    by remember { mutableStateOf(false) }
    var showEndTimePicker    by remember { mutableStateOf(false) }
    var showLocationDialog   by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var locationDraft        by remember { mutableStateOf(state.location) }
    var descriptionDraft     by remember { mutableStateOf(state.description) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        PxTextField(
            value         = state.title,
            onValueChange = { onEvent(AddEditEventUiEvent.TitleChanged(it)) },
            placeholder   = stringResource(R.string.event_field_title),
            isError       = state.titleError != null,
            errorMessage  = state.titleError,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        SheetRow(icon = Icons.Outlined.Palette, label = stringResource(R.string.event_field_color)) {
            EventColorPicker(
                selectedHex     = state.color,
                onColorSelected = { onEvent(AddEditEventUiEvent.ColorSelected(it)) }
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon    = Icons.Outlined.CalendarMonth,
            label   = stringResource(R.string.event_field_start),
            onClick = { showStartDatePicker = true }
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = dateFormatter.format(Instant.ofEpochMilli(state.startMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEEEEF5)
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888899))
                    Text(
                        text  = timeFormatter.format(Instant.ofEpochMilli(state.startMs)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEEEEF5)
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        SheetRow(
            icon    = Icons.Outlined.AccessTime,
            label   = stringResource(R.string.event_field_end),
            onClick = { showEndDatePicker = true }
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = dateFormatter.format(Instant.ofEpochMilli(state.endMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEEEEF5)
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888899))
                    Text(
                        text  = timeFormatter.format(Instant.ofEpochMilli(state.endMs)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEEEEF5)
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        SheetRow(icon = Icons.Outlined.WbSunny, label = stringResource(R.string.event_field_all_day)) {
            Switch(
                checked         = state.isAllDay,
                onCheckedChange = { onEvent(AddEditEventUiEvent.AllDayToggled(it)) },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = Color(0xFF6366F1),
                    uncheckedThumbColor = Color(0xFF888899),
                    uncheckedTrackColor = Color(0xFF252533)
                )
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon    = Icons.Outlined.LocationOn,
            label   = stringResource(R.string.event_field_location),
            onClick = {
                locationDraft = state.location
                showLocationDialog = true
            }
        ) {
            Text(
                text  = state.location.ifBlank { stringResource(R.string.event_field_location_hint) },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.location.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        SheetRow(
            icon    = Icons.Outlined.Notes,
            label   = stringResource(R.string.event_field_description),
            onClick = {
                descriptionDraft = state.description
                showDescriptionDialog = true
            }
        ) {
            Text(
                text  = state.description.ifBlank { stringResource(R.string.event_field_description_hint) },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.description.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon    = Icons.Outlined.Repeat,
            label   = stringResource(R.string.event_field_recurrence),
            onClick = { showRecurrencePicker = !showRecurrencePicker }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = recurrenceLabel(state.recurrenceRule),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        if (showRecurrencePicker) {
            RecurrencePickerRow(
                current  = state.recurrenceRule,
                onSelect = { rule ->
                    onEvent(AddEditEventUiEvent.RecurrenceRuleChanged(rule))
                    showRecurrencePicker = false
                }
            )
        }

        SheetRow(
            icon    = Icons.Outlined.NotificationsActive,
            label   = stringResource(R.string.event_field_reminder),
            onClick = { showReminderPicker = !showReminderPicker }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = reminderLabel(state.reminderMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        if (showReminderPicker) {
            ReminderPickerRow(
                current  = state.reminderMinutes,
                onSelect = { minutes ->
                    onEvent(AddEditEventUiEvent.ReminderMinutesChanged(minutes))
                    showReminderPicker = false
                }
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text  = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFEF4444)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (state.eventId != null) {
                TextButton(onClick = { onEvent(AddEditEventUiEvent.Delete) }) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint               = Color(0xFFEF4444),
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.delete), color = Color(0xFFEF4444), style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            PxButton(
                text      = if (state.eventId == null) stringResource(R.string.create) else stringResource(R.string.save),
                onClick   = { onEvent(AddEditEventUiEvent.Save) },
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    // Start Date Picker
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showStartDatePicker = false
                    if (!state.isAllDay) showStartTimePicker = true
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.startMs
            )
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val oldInstant = Instant.ofEpochMilli(state.startMs)
                    val localTime = LocalTime.ofInstant(oldInstant, ZoneId.systemDefault())
                    val newInstant = Instant.ofEpochMilli(millis)
                    val newDate = LocalDate.ofInstant(newInstant, ZoneId.systemDefault())
                    val combined = newDate.atTime(localTime).toInstant(ZoneOffset.UTC)
                    onEvent(AddEditEventUiEvent.StartDateTimeChanged(combined.toEpochMilli()))
                }
            }
        }
    }

    // Start Time Picker
    if (showStartTimePicker) {
        val instant = Instant.ofEpochMilli(state.startMs)
        val localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault())
        val timePickerState = rememberTimePickerState(
            initialHour = localTime.hour,
            initialMinute = localTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text(stringResource(R.string.dialog_time_picker_title), color = Color.White) },
            text = { TimePicker(state = timePickerState) },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val combined = date.atTime(newTime).toInstant(ZoneOffset.UTC)
                    onEvent(AddEditEventUiEvent.StartDateTimeChanged(combined.toEpochMilli()))
                    showStartTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // End Date Picker
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showEndDatePicker = false
                    if (!state.isAllDay) showEndTimePicker = true
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.endMs
            )
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val oldInstant = Instant.ofEpochMilli(state.endMs)
                    val localTime = LocalTime.ofInstant(oldInstant, ZoneId.systemDefault())
                    val newInstant = Instant.ofEpochMilli(millis)
                    val newDate = LocalDate.ofInstant(newInstant, ZoneId.systemDefault())
                    val combined = newDate.atTime(localTime).toInstant(ZoneOffset.UTC)
                    onEvent(AddEditEventUiEvent.EndDateTimeChanged(combined.toEpochMilli()))
                }
            }
        }
    }

    // End Time Picker
    if (showEndTimePicker) {
        val instant = Instant.ofEpochMilli(state.endMs)
        val localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault())
        val timePickerState = rememberTimePickerState(
            initialHour = localTime.hour,
            initialMinute = localTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text(stringResource(R.string.dialog_time_picker_title), color = Color.White) },
            text = { TimePicker(state = timePickerState) },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val combined = date.atTime(newTime).toInstant(ZoneOffset.UTC)
                    onEvent(AddEditEventUiEvent.EndDateTimeChanged(combined.toEpochMilli()))
                    showEndTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(stringResource(R.string.event_field_location), color = Color.White) },
            text = {
                TextField(
                    value       = locationDraft,
                    onValueChange = { locationDraft = it },
                    placeholder  = { Text(stringResource(R.string.event_field_location_hint)) },
                    singleLine   = true
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.LocationChanged(locationDraft))
                    showLocationDialog = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Description Dialog
    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text(stringResource(R.string.event_field_description), color = Color.White) },
            text = {
                TextField(
                    value       = descriptionDraft,
                    onValueChange = { descriptionDraft = it },
                    placeholder  = { Text(stringResource(R.string.event_field_description_hint)) }
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.DescriptionChanged(descriptionDraft))
                    showDescriptionDialog = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDescriptionDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SheetRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Color(0xFF888899),
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = Color(0xFFCCCCD8),
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
private fun RecurrencePickerRow(
    current: String?,
    onSelect: (String?) -> Unit
) {
    val options = listOf(
        null           to stringResource(R.string.none),
        "FREQ=DAILY"   to stringResource(R.string.event_recurrence_daily),
        "FREQ=WEEKLY"  to stringResource(R.string.event_recurrence_weekly),
        "FREQ=MONTHLY" to stringResource(R.string.event_recurrence_monthly)
    )
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (rule, label) ->
            RecurrenceChip(
                label      = label,
                isSelected = current == rule,
                onClick    = { onSelect(rule) }
            )
        }
    }
}

@Composable
private fun ReminderPickerRow(
    current: Int?,
    onSelect: (Int?) -> Unit
) {
    val options = listOf(
        null to stringResource(R.string.none),
        5    to stringResource(R.string.event_reminder_5_min),
        10   to stringResource(R.string.event_reminder_10_min),
        30   to stringResource(R.string.event_reminder_30_min),
        60   to stringResource(R.string.event_reminder_1_hour)
    )
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (minutes, label) ->
            RecurrenceChip(
                label      = label,
                isSelected = current == minutes,
                onClick    = { onSelect(minutes) }
            )
        }
    }
}

@Composable
private fun recurrenceLabel(rule: String?) = when (rule) {
    null           -> stringResource(R.string.event_recurrence_none)
    "FREQ=DAILY"   -> stringResource(R.string.event_recurrence_daily)
    "FREQ=WEEKLY"  -> stringResource(R.string.event_recurrence_weekly)
    "FREQ=MONTHLY" -> stringResource(R.string.event_recurrence_monthly)
    else           -> stringResource(R.string.event_recurrence_custom)
}

@Composable
private fun reminderLabel(minutes: Int?) = when (minutes) {
    null -> stringResource(R.string.none)
    5    -> stringResource(R.string.event_reminder_5_min)
    10   -> stringResource(R.string.event_reminder_10_min)
    30   -> stringResource(R.string.event_reminder_30_min)
    60   -> stringResource(R.string.event_reminder_1_hour)
    else -> stringResource(R.string.none)
}
