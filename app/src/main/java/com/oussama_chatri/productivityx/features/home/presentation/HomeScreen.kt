package com.oussama_chatri.productivityx.features.home.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxLoadingState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PriorityColors
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.core.util.UiState
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import java.time.ZoneId

// Entry point — wired from HomeTab in AppNavGraph.kt
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState    = uiState.dashboardState,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        modifier       = modifier.fillMaxSize(),
        label          = "home_content",
    ) { state ->
        when (state) {
            is UiState.Loading -> PxLoadingState()
            is UiState.Error   -> HomeErrorState(message = state.message)
            is UiState.Success -> HomeContent(
                summary               = state.data,
                onNavigateToProfile   = onNavigateToProfile,
                onSeeAllTasks         = onNavigateToTasks,
                onSeeAllEvents        = onNavigateToCalendar,
                onSeeAllNotes         = onNavigateToNotes,
                onNavigateToPomodoro  = onNavigateToPomodoro,
            )
        }
    }
}

// Main scrollable content

@Composable
private fun HomeContent(
    summary: DashboardSummary,
    onNavigateToProfile: () -> Unit,
    onSeeAllTasks: () -> Unit,
    onSeeAllEvents: () -> Unit,
    onSeeAllNotes: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
) {
    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item { SummaryCardsRow(summary = summary) }

        item { Spacer(Modifier.height(12.dp)) }

        item { FocusStrip(focusMinutes = summary.todayFocusMinutes, onOpenPomodoro = onNavigateToPomodoro) }

        item { Spacer(Modifier.height(24.dp)) }

        // Due Today
        item {
            SectionHeader(title = stringResource(R.string.home_due_today), actionLabel = stringResource(R.string.see_all), onAction = onSeeAllTasks)
            Spacer(Modifier.height(4.dp))
        }

        if (summary.dueTodayTasks.isNotEmpty()) {
            items(summary.dueTodayTasks, key = { it.id }) { task ->
                TaskRow(task = task)
                Spacer(Modifier.height(4.dp))
            }
        } else {
            item { EmptySection(label = stringResource(R.string.home_no_tasks_today)) }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // Upcoming Events
        item {
            SectionHeader(title = stringResource(R.string.home_upcoming_events), actionLabel = stringResource(R.string.see_all), onAction = onSeeAllEvents)
            Spacer(Modifier.height(4.dp))
        }

        if (summary.upcomingEvents.isNotEmpty()) {
            items(summary.upcomingEvents.take(3), key = { it.id }) { event ->
                EventRow(event = event)
                Spacer(Modifier.height(4.dp))
            }
        } else {
            item { EmptySection(label = stringResource(R.string.home_no_events)) }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // Recent Notes
        item {
            SectionHeader(title = stringResource(R.string.home_recent_notes), actionLabel = stringResource(R.string.see_all), onAction = onSeeAllNotes)
            Spacer(Modifier.height(8.dp))
        }

        item {
            if (summary.recentNotes.isNotEmpty()) {
                RecentNotesRow(notes = summary.recentNotes)
            } else {
                EmptySection(label = stringResource(R.string.home_no_notes))
            }
        }
    }
}

// Summary stat cards

@Composable
private fun SummaryCardsRow(summary: DashboardSummary) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SummaryCard(
                icon  = Icons.Outlined.CheckCircle,
                tint  = PxColors.Primary,
                count = summary.tasksDueToday.toString(),
                label = stringResource(R.string.home_due_today),
            )
        }
        item {
            SummaryCard(
                icon  = Icons.Outlined.AccessAlarm,
                tint  = PxColors.Error,
                count = summary.tasksOverdue.toString(),
                label = stringResource(R.string.home_overdue),
            )
        }
        item {
            SummaryCard(
                icon  = Icons.Outlined.CalendarMonth,
                tint  = PxColors.Secondary,
                count = summary.upcomingEvents.size.toString(),
                label = stringResource(R.string.home_upcoming_events),
            )
        }
        item {
            SummaryCard(
                icon  = Icons.Outlined.StickyNote2,
                tint  = PxColors.Info,
                count = summary.totalActiveNotes.toString(),
                label = stringResource(R.string.home_total_notes),
            )
        }
    }
}

@Composable
private fun SummaryCard(icon: ImageVector, tint: Color, count: String, label: String) {
    Column(
        modifier            = Modifier
            .size(width = 100.dp, height = 96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(20.dp),
        )
        Column {
            Text(
                text  = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Today's focus strip

@Composable
private fun FocusStrip(focusMinutes: Int, onOpenPomodoro: () -> Unit = {}) {
    val targetMinutes = 120f
    val progress      = (focusMinutes / targetMinutes).coerceIn(0f, 1f)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onOpenPomodoro() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Outlined.Timer,
            contentDescription = null,
            tint               = PxColors.Warning,
            modifier           = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = stringResource(R.string.home_focus_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = DateTimeUtils.focusDurationLabel(focusMinutes),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.width(16.dp))
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(CircleShape),
            color      = PxColors.Primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// Section helpers

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TextButton(onClick = onAction) {
            Text(
                text  = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = PxColors.Primary,
            )
        }
    }
}

@Composable
private fun EmptySection(label: String) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Task row

@Composable
private fun TaskRow(task: Task) {
    val priorityColor = when (task.priority.name) {
        "LOW"    -> PriorityColors.Low
        "HIGH"   -> PriorityColors.High
        "URGENT" -> PriorityColors.Urgent
        else     -> PriorityColors.Medium
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint               = PxColors.Primary,
            modifier           = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = task.title,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val timeLabel = task.dueTime?.let { time ->
                DateTimeUtils.formatTime(
                    task.dueDate!!.atTime(time)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                )
            } ?: stringResource(R.string.today)
            Text(
                text  = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(priorityColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text  = task.priority.name,
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor,
            )
        }
    }
}

// Event row

@SuppressLint("UseKtx")
@Composable
private fun EventRow(event: Event) {
    val eventColor = runCatching { Color(event.color.toColorInt()) }
        .getOrDefault(PxColors.Primary)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(58.dp)
                .background(
                    color = eventColor,
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding( end = 12.dp, start = 0.dp,
                ),
        ) {
            Text(
                text     = event.title,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.EventAvailable,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = "${DateTimeUtils.formatTime(event.startAt)} – ${DateTimeUtils.formatTime(event.endAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                event.location?.let { loc ->
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.Outlined.Place,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text     = loc,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// Recent notes horizontal row

@Composable
private fun RecentNotesRow(notes: List<Note>) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note = note)
        }
    }
}

@Composable
private fun NoteCard(note: Note) {
    Column(
        modifier = Modifier
            .size(width = 180.dp, height = 130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        Text(
            text     = note.title.ifBlank { stringResource(R.string.notes_untitled) },
            style    = MaterialTheme.typography.titleMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = note.preview,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (note.tags.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(note.tags.toList().take(2)) { tag ->
                    val tagColor = runCatching {
                        Color(android.graphics.Color.parseColor(tag.color))
                    }.getOrDefault(PxColors.Primary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(tagColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text  = tag.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                        )
                    }
                }
            }
        }
    }
}

// Error state

@Composable
private fun HomeErrorState(message: String) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text      = stringResource(R.string.empty_state_error_title),
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = message,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
