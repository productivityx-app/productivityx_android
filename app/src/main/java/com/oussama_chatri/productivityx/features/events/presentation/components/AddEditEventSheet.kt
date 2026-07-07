package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.speech.RecognizerIntent
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.state.AddEditEventUiState
import com.oussama_chatri.productivityx.features.events.presentation.state.EventTemplateType
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
    prefilledEndMs: Long? = null,
    onDismiss: () -> Unit,
    viewModel: AddEditEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(eventId, prefilledStartMs, prefilledEndMs) {
        viewModel.init(eventId, prefilledStartMs, prefilledEndMs)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> onDismiss()
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
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF252533))
            )
        }
    ) {
        AddEditEventSheetContent(
            state = state,
            onEvent = viewModel::onEvent,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditEventSheetContent(
    state: AddEditEventUiState,
    onEvent: (AddEditEventUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    var showRecurrencePicker by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showAttendeeDialog by remember { mutableStateOf(false) }
    var showMeetingUrlDialog by remember { mutableStateOf(false) }
    var showTravelTimeDialog by remember { mutableStateOf(false) }
    var locationDraft by remember { mutableStateOf(state.location) }
    var descriptionDraft by remember { mutableStateOf(state.description) }
    var attendeeDraft by remember { mutableStateOf("") }
    var meetingUrlDraft by remember { mutableStateOf(state.meetingUrl ?: "") }
    var travelTimeDraft by remember { mutableStateOf(state.travelTimeMinutes?.toString() ?: "") }
    val focusManager = LocalFocusManager.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull() ?: ""
        if (text.isNotBlank()) {
            onEvent(AddEditEventUiEvent.TitleChanged(text))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        if (state.eventId == null) {
            TemplateQuickApply(
                currentTemplate = state.eventTemplate,
                onTemplateSelected = { onEvent(AddEditEventUiEvent.TemplateApplied(it)) }
            )
            Spacer(Modifier.height(12.dp))
        }

        if (state.titleSuggestions.isNotEmpty() && state.title.isBlank() && state.eventId == null) {
            SmartTitleSuggestions(
                suggestions = state.titleSuggestions,
                onSuggestion = { onEvent(AddEditEventUiEvent.TitleSuggestionAccepted(it)) }
            )
            Spacer(Modifier.height(8.dp))
        }

        PxTextField(
            value = state.title,
            onValueChange = { onEvent(AddEditEventUiEvent.TitleChanged(it)) },
            placeholder = stringResource(R.string.event_field_title),
            isError = state.titleError != null,
            errorMessage = state.titleError,
            modifier = Modifier.fillMaxWidth(),
            onVoiceInput = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the event title")
                }
                try {
                    speechLauncher.launch(intent)
                } catch (_: Exception) { }
            }
        )

        Spacer(Modifier.height(12.dp))

        if (state.showConflictWarning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Time conflict with ${state.conflictingEvents.size} event(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        SheetRow(icon = Icons.Outlined.Palette, label = stringResource(R.string.event_field_color)) {
            EventColorPicker(
                selectedHex = state.color,
                onColorSelected = { onEvent(AddEditEventUiEvent.ColorSelected(it)) }
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon = Icons.Outlined.CalendarMonth,
            label = stringResource(R.string.event_field_start),
            onClick = { showStartDatePicker = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = runCatching { dateFormatter.format(Instant.ofEpochMilli(state.startMs)) }.getOrElse { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEEEEF5)
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888899))
                    Text(
                        text = runCatching { timeFormatter.format(Instant.ofEpochMilli(state.startMs)) }.getOrElse { "-" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEEEEF5)
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        SheetRow(
            icon = Icons.Outlined.AccessTime,
            label = stringResource(R.string.event_field_end),
            onClick = { showEndDatePicker = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = runCatching { dateFormatter.format(Instant.ofEpochMilli(state.endMs)) }.getOrElse { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEEEEF5)
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888899))
                    Text(
                        text = runCatching { timeFormatter.format(Instant.ofEpochMilli(state.endMs)) }.getOrElse { "-" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEEEEF5)
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        SheetRow(icon = Icons.Outlined.WbSunny, label = stringResource(R.string.event_field_all_day)) {
            Switch(
                checked = state.isAllDay,
                onCheckedChange = { onEvent(AddEditEventUiEvent.AllDayToggled(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6366F1),
                    uncheckedThumbColor = Color(0xFF888899),
                    uncheckedTrackColor = Color(0xFF252533)
                )
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon = Icons.Outlined.LocationOn,
            label = stringResource(R.string.event_field_location),
            onClick = {
                locationDraft = state.location
                showLocationDialog = true
            }
        ) {
            Text(
                text = state.location.ifBlank { "Add location" },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.location.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        SheetRow(
            icon = Icons.Outlined.Notes,
            label = stringResource(R.string.event_field_description),
            onClick = {
                descriptionDraft = state.description
                showDescriptionDialog = true
            }
        ) {
            Text(
                text = state.description.ifBlank { "Add description" },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.description.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        SheetRow(
            icon = Icons.Outlined.Person,
            label = "Attendees",
            onClick = { showAttendeeDialog = true }
        ) {
            if (state.attendees.isNotEmpty()) {
                Text(
                    text = "${state.attendees.size} attendee(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEEEEF5)
                )
            } else {
                Text(
                    text = "Add attendees",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
            }
        }

        SheetRow(
            icon = Icons.Outlined.Videocam,
            label = "Meeting link",
            onClick = {
                meetingUrlDraft = state.meetingUrl ?: ""
                showMeetingUrlDialog = true
            }
        ) {
            Text(
                text = state.meetingUrl?.take(30)?.plus("...") ?: "Add link",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.meetingUrl != null) Color(0xFFEEEEF5) else Color(0xFF888899)
            )
        }

        SheetRow(
            icon = Icons.Outlined.Schedule,
            label = "Travel time",
            onClick = {
                travelTimeDraft = state.travelTimeMinutes?.toString() ?: ""
                showTravelTimeDialog = true
            }
        ) {
            Text(
                text = if (state.travelTimeMinutes != null) "${state.travelTimeMinutes} min" else "Add estimate",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.travelTimeMinutes != null) Color(0xFFEEEEF5) else Color(0xFF888899)
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon = Icons.Outlined.Repeat,
            label = stringResource(R.string.event_field_recurrence),
            onClick = { showRecurrencePicker = !showRecurrencePicker }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = recurrenceLabel(state.recurrenceRule),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        if (showRecurrencePicker) {
            RecurrencePickerRow(
                current = state.recurrenceRule,
                onSelect = { rule ->
                    onEvent(AddEditEventUiEvent.RecurrenceRuleChanged(rule))
                    showRecurrencePicker = false
                }
            )
        }

        SheetRow(
            icon = Icons.Outlined.NotificationsActive,
            label = stringResource(R.string.event_field_reminder),
            onClick = { showReminderPicker = !showReminderPicker }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val reminderLabel = if (state.reminderTimes.isNotEmpty()) {
                    "Multiple (${state.reminderTimes.size})"
                } else {
                    reminderLabel(state.reminderMinutes)
                }
                Text(
                    text = reminderLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
            }
        }

        if (showReminderPicker) {
            MultipleReminderPicker(
                selectedMinutes = state.reminderTimes,
                onToggle = { minutes ->
                    if (minutes in state.reminderTimes) {
                        onEvent(AddEditEventUiEvent.RemoveReminderTime(minutes))
                    } else {
                        onEvent(AddEditEventUiEvent.AddReminderTime(minutes))
                    }
                }
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFEF4444)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.eventId != null) {
                TextButton(onClick = { onEvent(AddEditEventUiEvent.Delete) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.delete), color = Color(0xFFEF4444), style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            PxButton(
                text = if (state.eventId == null) stringResource(R.string.create) else stringResource(R.string.save),
                onClick = {
                    onEvent(AddEditEventUiEvent.CheckConflicts)
                    onEvent(AddEditEventUiEvent.Save)
                },
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showStartDatePicker = false
                    if (!state.isAllDay) showStartTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startMs)
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val result = runCatching {
                        val oldInstant = Instant.ofEpochMilli(state.startMs)
                        val localTime = LocalTime.ofInstant(oldInstant, ZoneId.systemDefault())
                        val newInstant = Instant.ofEpochMilli(millis)
                        val newDate = LocalDate.ofInstant(newInstant, ZoneId.systemDefault())
                        newDate.atTime(localTime).toInstant(ZoneOffset.UTC).toEpochMilli()
                    }.getOrNull()
                    if (result != null) onEvent(AddEditEventUiEvent.StartDateTimeChanged(result))
                }
            }
        }
    }

    if (showStartTimePicker) {
        val safeInstant = runCatching { Instant.ofEpochMilli(state.startMs) }.getOrNull()
        if (safeInstant != null) {
            val localTime = LocalTime.ofInstant(safeInstant, ZoneId.systemDefault())
            val timePickerState = rememberTimePickerState(
                initialHour = localTime.hour,
                initialMinute = localTime.minute,
                is24Hour = false
            )
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                title = { Text("Select time", color = Color.White) },
                text = { TimePicker(state = timePickerState) },
                containerColor = Color(0xFF1A1A24),
                confirmButton = {
                    TextButton(onClick = {
                        val date = LocalDate.ofInstant(safeInstant, ZoneId.systemDefault())
                        val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val combined = date.atTime(newTime).toInstant(ZoneOffset.UTC)
                        onEvent(AddEditEventUiEvent.StartDateTimeChanged(combined.toEpochMilli()))
                        showStartTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                }
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showEndDatePicker = false
                    if (!state.isAllDay) showEndTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.endMs)
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val result = runCatching {
                        val oldInstant = Instant.ofEpochMilli(state.endMs)
                        val localTime = LocalTime.ofInstant(oldInstant, ZoneId.systemDefault())
                        val newInstant = Instant.ofEpochMilli(millis)
                        val newDate = LocalDate.ofInstant(newInstant, ZoneId.systemDefault())
                        newDate.atTime(localTime).toInstant(ZoneOffset.UTC).toEpochMilli()
                    }.getOrNull()
                    if (result != null) onEvent(AddEditEventUiEvent.EndDateTimeChanged(result))
                }
            }
        }
    }

    if (showEndTimePicker) {
        val safeEndInstant = runCatching { Instant.ofEpochMilli(state.endMs) }.getOrNull()
        if (safeEndInstant != null) {
            val localTime = LocalTime.ofInstant(safeEndInstant, ZoneId.systemDefault())
            val timePickerState = rememberTimePickerState(
                initialHour = localTime.hour,
                initialMinute = localTime.minute,
                is24Hour = false
            )
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                title = { Text("Select time", color = Color.White) },
                text = { TimePicker(state = timePickerState) },
                containerColor = Color(0xFF1A1A24),
                confirmButton = {
                    TextButton(onClick = {
                        val date = LocalDate.ofInstant(safeEndInstant, ZoneId.systemDefault())
                        val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val combined = date.atTime(newTime).toInstant(ZoneOffset.UTC)
                        onEvent(AddEditEventUiEvent.EndDateTimeChanged(combined.toEpochMilli()))
                        showEndTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                }
            )
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = locationDraft,
                    onValueChange = { locationDraft = it },
                    placeholder = { Text("Enter location") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1)
                    )
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.LocationChanged(locationDraft))
                    showLocationDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Description", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = descriptionDraft,
                    onValueChange = { descriptionDraft = it },
                    placeholder = { Text("Add notes about this event...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1)
                    ),
                    modifier = Modifier.height(120.dp)
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.DescriptionChanged(descriptionDraft))
                    showDescriptionDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDescriptionDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAttendeeDialog) {
        AlertDialog(
            onDismissRequest = { showAttendeeDialog = false },
            title = { Text("Attendees", color = Color.White) },
            text = {
                Column {
                    state.attendees.forEach { email ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFEEEEF5),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onEvent(AddEditEventUiEvent.RemoveAttendee(email)) }) {
                                Icon(Icons.Outlined.Close, "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = attendeeDraft,
                            onValueChange = { attendeeDraft = it },
                            placeholder = { Text("Email address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (attendeeDraft.isNotBlank()) {
                                    onEvent(AddEditEventUiEvent.AddAttendee(attendeeDraft.trim()))
                                    attendeeDraft = ""
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF6366F1)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (attendeeDraft.isNotBlank()) {
                                onEvent(AddEditEventUiEvent.AddAttendee(attendeeDraft.trim()))
                                attendeeDraft = ""
                            }
                        }) {
                            Icon(Icons.Outlined.Add, "Add", tint = Color(0xFF6366F1))
                        }
                    }
                }
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = { showAttendeeDialog = false }) { Text("Done") }
            }
        )
    }

    if (showMeetingUrlDialog) {
        AlertDialog(
            onDismissRequest = { showMeetingUrlDialog = false },
            title = { Text("Meeting link", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = meetingUrlDraft,
                    onValueChange = { meetingUrlDraft = it },
                    placeholder = { Text("https://meet.google.com/...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1)
                    )
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.MeetingUrlChanged(meetingUrlDraft))
                    showMeetingUrlDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showMeetingUrlDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showTravelTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTravelTimeDialog = false },
            title = { Text("Travel time", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = travelTimeDraft,
                    onValueChange = { travelTimeDraft = it },
                    placeholder = { Text("Minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1)
                    )
                )
            },
            containerColor = Color(0xFF1A1A24),
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.TravelTimeChanged(travelTimeDraft.toIntOrNull()))
                    showTravelTimeDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTravelTimeDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TemplateQuickApply(
    currentTemplate: EventTemplateType?,
    onTemplateSelected: (EventTemplateType) -> Unit,
) {
    val templates = listOf(
        EventTemplateType.MEETING to Icons.Outlined.Groups,
        EventTemplateType.FOCUS_TIME to Icons.Outlined.SelfImprovement,
        EventTemplateType.BREAK to Icons.Outlined.Work,
        EventTemplateType.TRAVEL to Icons.Outlined.DirectionsCar,
    )

    Column {
        Text(
            text = "Quick templates",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF888899),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            templates.forEach { (template, icon) ->
                val isSelected = currentTemplate == template
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Color(0xFF6366F1).copy(alpha = 0.2f)
                            else Color(0xFF252533)
                        )
                        .clickable { onTemplateSelected(template) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = template.label,
                            tint = if (isSelected) Color(0xFF6366F1) else Color(0xFF888899),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = template.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF6366F1) else Color(0xFF888899)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTitleSuggestions(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lightbulb, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888899)
            )
        }
        Spacer(Modifier.height(4.dp))
                    @OptIn(ExperimentalLayoutApi::class) FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF252533))
                        .clickable { onSuggestion(suggestion) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888899)
                    )
                }
            }
        }
    }
}

@Composable
private fun MultipleReminderPicker(
    selectedMinutes: List<Int>,
    onToggle: (Int) -> Unit,
) {
    val options = listOf(
        0 to "At time",
        5 to "5 min",
        10 to "10 min",
        15 to "15 min",
        30 to "30 min",
        60 to "1 hour",
        1440 to "1 day",
    )

    @OptIn(ExperimentalLayoutApi::class) FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        options.forEach { (minutes, label) ->
            val isSelected = minutes in selectedMinutes
            RecurrenceChip(
                label = label,
                isSelected = isSelected,
                onClick = { onToggle(minutes) }
            )
        }
    }
}

@Composable
private fun SheetRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF888899),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCCCCD8),
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
private fun RecurrencePickerRow(
    current: String?,
    onSelect: (String?) -> Unit,
) {
    val options = listOf(
        null to "None",
        "FREQ=DAILY" to "Daily",
        "FREQ=WEEKLY" to "Weekly",
        "FREQ=MONTHLY" to "Monthly",
        "FREQ=YEARLY" to "Yearly",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (rule, label) ->
            RecurrenceChip(
                label = label,
                isSelected = current == rule,
                onClick = { onSelect(rule) }
            )
        }
    }
}

@Composable
private fun RecurrenceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6366F1) else Color(0xFF252533),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF888899),
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
private fun recurrenceLabel(rule: String?) = when (rule) {
    null -> "None"
    "FREQ=DAILY" -> "Daily"
    "FREQ=WEEKLY" -> "Weekly"
    "FREQ=MONTHLY" -> "Monthly"
    "FREQ=YEARLY" -> "Yearly"
    else -> "Custom"
}

@Composable
private fun reminderLabel(minutes: Int?) = when (minutes) {
    null -> "None"
    0 -> "At time"
    5 -> "5 min before"
    10 -> "10 min before"
    15 -> "15 min before"
    30 -> "30 min before"
    60 -> "1 hour before"
    1440 -> "1 day before"
    else -> "Custom"
}
