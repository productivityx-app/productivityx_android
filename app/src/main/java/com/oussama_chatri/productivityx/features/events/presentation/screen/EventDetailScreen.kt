package com.oussama_chatri.productivityx.features.events.presentation.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.event.CalendarUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.AddEditEventViewModel
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val fullDateTimeFormatter = DateTimeFormatter
    .ofPattern("EEEE, MMMM d, yyyy  ·  h:mm a")
    .withZone(ZoneId.systemDefault())
private val fullDateFormatter = DateTimeFormatter
    .ofPattern("EEEE, MMMM d, yyyy")
    .withZone(ZoneId.systemDefault())
private val timeOnlyFormatter = DateTimeFormatter
    .ofPattern("h:mm a")
    .withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: AddEditEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(eventId) { viewModel.init(eventId, null) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = PxColors.OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString("${state.title} - ${state.location}"))
                    }) {
                        Icon(Icons.Outlined.Share, "Share", tint = PxColors.OnSurface)
                    }
                    IconButton(onClick = { onEdit(eventId) }) {
                        Icon(Icons.Outlined.Edit, "Edit", tint = PxColors.OnSurface)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = PxColors.Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val eventColor = runCatching {
                Color(android.graphics.Color.parseColor(state.color))
            }.getOrDefault(PxColors.Primary)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(eventColor, eventColor.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)) {
                    Text(
                        text = state.title.ifBlank { "Untitled Event" },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (state.eventTemplate != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.eventTemplate!!.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                DetailCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (state.isAllDay) {
                            DetailRow(
                                icon = Icons.Outlined.WbSunny,
                                label = runCatching { fullDateFormatter.format(Instant.ofEpochMilli(state.startMs)) }
                                    .getOrElse { "-" }
                            )
                        } else {
                            DetailRow(
                                icon = Icons.Outlined.CalendarMonth,
                                label = runCatching { fullDateTimeFormatter.format(Instant.ofEpochMilli(state.startMs)) }
                                    .getOrElse { "-" }
                            )
                            DetailRow(
                                icon = Icons.Outlined.AccessTime,
                                label = "Ends ${runCatching { timeOnlyFormatter.format(Instant.ofEpochMilli(state.endMs)) }.getOrElse { "-" }}"
                            )
                            DetailRow(
                                icon = Icons.Outlined.Schedule,
                                label = "Timezone: ${ZoneId.systemDefault().id}"
                            )
                        }

                        if (state.location.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.LocationOn,
                                label = state.location,
                                trailing = {
                                    Icon(
                                        Icons.Outlined.NearMe,
                                        contentDescription = "Navigate",
                                        tint = PxColors.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        if (state.description.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(icon = Icons.Outlined.Notes, label = state.description)
                        }

                        if (state.recurrenceRule != null) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.Repeat,
                                label = recurrenceLabelFromRule(state.recurrenceRule)
                            )
                        }

                        if (state.reminderMinutes != null) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.NotificationsActive,
                                label = "${state.reminderMinutes} min before"
                            )
                        }

                        if (state.reminderTimes.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.NotificationsActive,
                                label = "Reminders: ${state.reminderTimes.joinToString(", ") { "${it}min" }}"
                            )
                        }

                        if (state.attendees.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.Person,
                                label = "${state.attendees.size} attendees"
                            )
                        }

                        if (state.travelTimeMinutes != null) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.Schedule,
                                label = "Travel time: ${state.travelTimeMinutes} min"
                            )
                        }

                        if (state.meetingUrl != null) {
                            Spacer(Modifier.height(2.dp))
                            DetailRow(
                                icon = Icons.Outlined.Videocam,
                                label = state.meetingUrl!!,
                                trailing = {
                                    Icon(
                                        Icons.Outlined.Videocam,
                                        contentDescription = "Join",
                                        tint = PxColors.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                ActionButtonsRow(
                    onDuplicate = { /* TODO: duplicate logic */ },
                    onJoinMeeting = if (state.meetingUrl != null) {{ /* TODO: open URL */ }} else null,
                    onNavigate = if (state.location.isNotBlank()) {{ /* TODO: open maps */ }} else null,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = PxColors.Surface,
            title = {
                Text("Delete event?", color = PxColors.OnBackground, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text("This action cannot be undone.", color = PxColors.OnSurfaceDim, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.onEvent(AddEditEventUiEvent.Delete)
                }) {
                    Text("Delete", color = PxColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PxColors.Surface)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PxColors.OnSurfaceDim,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onDuplicate: () -> Unit,
    onJoinMeeting: (() -> Unit)?,
    onNavigate: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionChip(
            icon = Icons.Outlined.ContentCopy,
            label = "Duplicate",
            onClick = onDuplicate,
            modifier = Modifier.weight(1f)
        )
        if (onJoinMeeting != null) {
            ActionChip(
                icon = Icons.Outlined.Videocam,
                label = "Join Meeting",
                onClick = onJoinMeeting,
                modifier = Modifier.weight(1f)
            )
        }
        if (onNavigate != null) {
            ActionChip(
                icon = Icons.Outlined.NearMe,
                label = "Navigate",
                onClick = onNavigate,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PxColors.OnSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurface
            )
        }
    }
}

private fun recurrenceLabelFromRule(rule: String?) = when (rule) {
    "FREQ=DAILY" -> "Repeats daily"
    "FREQ=WEEKLY" -> "Repeats weekly"
    "FREQ=MONTHLY" -> "Repeats monthly"
    "FREQ=YEARLY" -> "Repeats yearly"
    else -> "Custom recurrence"
}
