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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.AddEditEventViewModel
import java.time.Instant
import java.time.ZoneId
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

@Composable
private fun AddEditEventSheetContent(
    state: com.oussama_chatri.productivityx.features.events.presentation.state.AddEditEventUiState,
    onEvent: (AddEditEventUiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var showRecurrencePicker by remember { mutableStateOf(false) }
    var showReminderPicker   by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Title — textStyle removed; PxTextField does not expose that parameter
        PxTextField(
            value         = state.title,
            onValueChange = { onEvent(AddEditEventUiEvent.TitleChanged(it)) },
            placeholder   = "Event title",
            isError       = state.titleError != null,
            errorMessage  = state.titleError,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        SheetRow(icon = Icons.Outlined.Palette, label = "Color") {
            EventColorPicker(
                selectedHex     = state.color,
                onColorSelected = { onEvent(AddEditEventUiEvent.ColorSelected(it)) }
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon    = Icons.Outlined.CalendarMonth,
            label   = "Start",
            onClick = { /* date/time picker */ }
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
            label   = "End",
            onClick = { /* date/time picker */ }
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

        SheetRow(icon = Icons.Outlined.WbSunny, label = "All day") {
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

        SheetRow(icon = Icons.Outlined.LocationOn, label = "Location") {
            Text(
                text  = state.location.ifBlank { "Add location" },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.location.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        SheetRow(icon = Icons.Outlined.Notes, label = "Description") {
            Text(
                text  = state.description.ifBlank { "Add description" },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.description.isBlank()) Color(0xFF888899) else Color(0xFFEEEEF5)
            )
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 4.dp))

        SheetRow(
            icon    = Icons.Outlined.Repeat,
            label   = "Repeat",
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
            label   = "Reminder",
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
                    Text("Delete", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            PxButton(
                text      = if (state.eventId == null) "Create" else "Save",
                onClick   = { onEvent(AddEditEventUiEvent.Save) },
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.height(8.dp))
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
        null           to "None",
        "FREQ=DAILY"   to "Daily",
        "FREQ=WEEKLY"  to "Weekly",
        "FREQ=MONTHLY" to "Monthly"
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
    val options = listOf(null to "None", 5 to "5 min", 10 to "10 min", 30 to "30 min", 60 to "1 hour")
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

private fun recurrenceLabel(rule: String?) = when (rule) {
    null           -> "Does not repeat"
    "FREQ=DAILY"   -> "Daily"
    "FREQ=WEEKLY"  -> "Weekly"
    "FREQ=MONTHLY" -> "Monthly"
    else           -> "Custom"
}

private fun reminderLabel(minutes: Int?) = when (minutes) {
    null -> "None"
    5    -> "5 min before"
    10   -> "10 min before"
    30   -> "30 min before"
    60   -> "1 hour before"
    else -> "$minutes min before"
}