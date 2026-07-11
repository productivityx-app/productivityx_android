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
        containerColor = PxColors.SurfaceVariant,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(PxColors.SurfaceVariant)
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

        val speakPrompt = stringResource(R.string.event_speak_prompt)
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
                    putExtra(RecognizerIntent.EXTRA_PROMPT, speakPrompt)
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
                        text = stringResource(R.string.event_conflict, state.conflictingEvents.size),
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

        HorizontalDivider(color = PxColors.Outline, modifier = Modifier.padding(vertical = 4.dp))

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
                    color = PxColors.OnBackground
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurfaceDim)
                    Text(
                        text = runCatching { timeFormatter.format(Instant.ofEpochMilli(state.startMs)) }.getOrElse { "-" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnBackground
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
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
                    color = PxColors.OnBackground
                )
                if (!state.isAllDay) {
                    Text("·", style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurfaceDim)
                    Text(
                        text = runCatching { timeFormatter.format(Instant.ofEpochMilli(state.endMs)) }.getOrElse { "-" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnBackground
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
            }
        }

        SheetRow(icon = Icons.Outlined.WbSunny, label = stringResource(R.string.event_field_all_day)) {
            Switch(
                checked = state.isAllDay,
                onCheckedChange = { onEvent(AddEditEventUiEvent.AllDayToggled(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PxColors.Primary,
                    uncheckedThumbColor = PxColors.OnSurfaceDim,
                    uncheckedTrackColor = PxColors.SurfaceVariant
                )
            )
        }

        HorizontalDivider(color = PxColors.Outline, modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon = Icons.Outlined.LocationOn,
            label = stringResource(R.string.event_field_location),
            onClick = {
                locationDraft = state.location
                showLocationDialog = true
            }
        ) {
            Text(
                text = state.location.ifBlank { stringResource(R.string.event_add_location) },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.location.isBlank()) PxColors.OnSurfaceDim else PxColors.OnBackground
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
                text = state.description.ifBlank { stringResource(R.string.event_add_description) },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.description.isBlank()) PxColors.OnSurfaceDim else PxColors.OnBackground
            )
        }

        SheetRow(
            icon = Icons.Outlined.Person,
            label = stringResource(R.string.event_attendees),
            onClick = { showAttendeeDialog = true }
        ) {
            if (state.attendees.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.event_attendee_count, state.attendees.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnBackground
                )
            } else {
                Text(
                    text = stringResource(R.string.event_add_attendees),
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim
                )
            }
        }

        SheetRow(
            icon = Icons.Outlined.Videocam,
            label = stringResource(R.string.event_meeting_link),
            onClick = {
                meetingUrlDraft = state.meetingUrl ?: ""
                showMeetingUrlDialog = true
            }
        ) {
            Text(
                text = state.meetingUrl?.take(30)?.plus("...") ?: stringResource(R.string.event_add_link),
                style = MaterialTheme.typography.bodySmall,
                color = if (state.meetingUrl != null) PxColors.OnBackground else PxColors.OnSurfaceDim
            )
        }

        SheetRow(
            icon = Icons.Outlined.Schedule,
            label = stringResource(R.string.event_travel_time),
            onClick = {
                travelTimeDraft = state.travelTimeMinutes?.toString() ?: ""
                showTravelTimeDialog = true
            }
        ) {
            Text(
                text = if (state.travelTimeMinutes != null) "${state.travelTimeMinutes} min" else stringResource(R.string.event_add_estimate),
                style = MaterialTheme.typography.bodySmall,
                color = if (state.travelTimeMinutes != null) PxColors.OnBackground else PxColors.OnSurfaceDim
            )
        }

        HorizontalDivider(color = PxColors.Outline, modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon = Icons.Outlined.Repeat,
            label = stringResource(R.string.event_field_recurrence),
            onClick = { showRecurrencePicker = !showRecurrencePicker }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = recurrenceLabel(state.recurrenceRule),
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
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
                    stringResource(R.string.event_reminder_multiple, state.reminderTimes.size)
                } else {
                    reminderLabel(state.reminderMinutes)
                }
                Text(
                    text = reminderLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim
                )
                Icon(Icons.Outlined.ChevronRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
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
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                title = { Text(stringResource(R.string.event_select_time), color = Color.White) },
                text = { TimePicker(state = timePickerState) },
                containerColor = PxColors.SurfaceVariant,
                confirmButton = {
                    TextButton(onClick = {
                        val date = LocalDate.ofInstant(safeInstant, ZoneId.systemDefault())
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
    }

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
                title = { Text(stringResource(R.string.event_select_time), color = Color.White) },
                text = { TimePicker(state = timePickerState) },
                containerColor = PxColors.SurfaceVariant,
                confirmButton = {
                    TextButton(onClick = {
                        val date = LocalDate.ofInstant(safeEndInstant, ZoneId.systemDefault())
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
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(stringResource(R.string.event_field_location), color = Color.White) },
            text = {
                OutlinedTextField(
                    value = locationDraft,
                    onValueChange = { locationDraft = it },
                    placeholder = { Text(stringResource(R.string.event_enter_location_hint)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PxColors.Primary
                    )
                )
            },
            containerColor = PxColors.SurfaceVariant,
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

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text(stringResource(R.string.event_field_description), color = Color.White) },
            text = {
                OutlinedTextField(
                    value = descriptionDraft,
                    onValueChange = { descriptionDraft = it },
                    placeholder = { Text(stringResource(R.string.event_field_description_hint)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PxColors.Primary
                    ),
                    modifier = Modifier.height(120.dp)
                )
            },
            containerColor = PxColors.SurfaceVariant,
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

    if (showAttendeeDialog) {
        AlertDialog(
            onDismissRequest = { showAttendeeDialog = false },
            title = { Text(stringResource(R.string.event_attendees), color = Color.White) },
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
                                color = PxColors.OnBackground,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onEvent(AddEditEventUiEvent.RemoveAttendee(email)) }) {
                                Icon(Icons.Outlined.Close, stringResource(R.string.remove), tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = attendeeDraft,
                            onValueChange = { attendeeDraft = it },
                            placeholder = { Text(stringResource(R.string.event_add_attendee_hint)) },
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
                                cursorColor = PxColors.Primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (attendeeDraft.isNotBlank()) {
                                onEvent(AddEditEventUiEvent.AddAttendee(attendeeDraft.trim()))
                                attendeeDraft = ""
                            }
                        }) {
                            Icon(Icons.Outlined.Add, stringResource(R.string.add), tint = PxColors.Primary)
                        }
                    }
                }
            },
            containerColor = PxColors.SurfaceVariant,
            confirmButton = {
                TextButton(onClick = { showAttendeeDialog = false }) { Text(stringResource(R.string.done)) }
            }
        )
    }

    if (showMeetingUrlDialog) {
        AlertDialog(
            onDismissRequest = { showMeetingUrlDialog = false },
            title = { Text(stringResource(R.string.event_meeting_link), color = Color.White) },
            text = {
                OutlinedTextField(
                    value = meetingUrlDraft,
                    onValueChange = { meetingUrlDraft = it },
                    placeholder = { Text(stringResource(R.string.event_meeting_url_hint)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PxColors.Primary
                    )
                )
            },
            containerColor = PxColors.SurfaceVariant,
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.MeetingUrlChanged(meetingUrlDraft))
                    showMeetingUrlDialog = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showMeetingUrlDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showTravelTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTravelTimeDialog = false },
            title = { Text(stringResource(R.string.event_travel_time), color = Color.White) },
            text = {
                OutlinedTextField(
                    value = travelTimeDraft,
                    onValueChange = { travelTimeDraft = it },
                    placeholder = { Text(stringResource(R.string.event_travel_minutes_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PxColors.Primary
                    )
                )
            },
            containerColor = PxColors.SurfaceVariant,
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AddEditEventUiEvent.TravelTimeChanged(travelTimeDraft.toIntOrNull()))
                    showTravelTimeDialog = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTravelTimeDialog = false }) { Text(stringResource(R.string.cancel)) }
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
            text = stringResource(R.string.event_quick_templates),
            style = MaterialTheme.typography.labelMedium,
            color = PxColors.OnSurfaceDim,
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
                            if (isSelected) PxColors.Primary.copy(alpha = 0.2f)
                            else PxColors.SurfaceVariant
                        )
                        .clickable { onTemplateSelected(template) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = template.label,
                            tint = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = template.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim
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
                text = stringResource(R.string.event_suggestions),
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
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
                        .background(PxColors.SurfaceVariant)
                        .clickable { onSuggestion(suggestion) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim
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
        0 to stringResource(R.string.event_reminder_short_at_time),
        5 to stringResource(R.string.event_reminder_short_min, 5),
        10 to stringResource(R.string.event_reminder_short_min, 10),
        15 to stringResource(R.string.event_reminder_short_min, 15),
        30 to stringResource(R.string.event_reminder_short_min, 30),
        60 to stringResource(R.string.event_reminder_short_1_hour),
        1440 to stringResource(R.string.event_reminder_short_1_day),
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
            tint = PxColors.OnSurfaceDim,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnSurface,
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
        null to stringResource(R.string.event_recurrence_none),
        "FREQ=DAILY" to stringResource(R.string.event_recurrence_daily),
        "FREQ=WEEKLY" to stringResource(R.string.event_recurrence_weekly),
        "FREQ=MONTHLY" to stringResource(R.string.event_recurrence_monthly),
        "FREQ=YEARLY" to stringResource(R.string.event_recurrence_yearly),
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
        targetValue = if (isSelected) PxColors.Primary else PxColors.SurfaceVariant,
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else PxColors.OnSurfaceDim,
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
    null -> stringResource(R.string.event_recurrence_none)
    "FREQ=DAILY" -> stringResource(R.string.event_recurrence_daily)
    "FREQ=WEEKLY" -> stringResource(R.string.event_recurrence_weekly)
    "FREQ=MONTHLY" -> stringResource(R.string.event_recurrence_monthly)
    "FREQ=YEARLY" -> stringResource(R.string.event_recurrence_yearly)
    else -> stringResource(R.string.event_recurrence_custom)
}

@Composable
private fun reminderLabel(minutes: Int?) = when (minutes) {
    null -> stringResource(R.string.event_reminder_none)
    0 -> stringResource(R.string.event_reminder_at_time)
    5 -> stringResource(R.string.event_reminder_5_min)
    10 -> stringResource(R.string.event_reminder_10_min)
    15 -> stringResource(R.string.event_reminder_15_min)
    30 -> stringResource(R.string.event_reminder_30_min)
    60 -> stringResource(R.string.event_reminder_1_hour)
    1440 -> stringResource(R.string.event_reminder_1_day)
    else -> stringResource(R.string.event_reminder_custom)
}
