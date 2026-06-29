package com.oussama_chatri.productivityx.features.home.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StickyNote2
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.core.ui.theme.PriorityColors
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import java.time.ZoneId

@Composable
fun TasksWidget(
    tasks: List<Task>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedCount = tasks.count { it.status.name == "DONE" }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    WidgetCard(
        title = "Today's Tasks",
        icon = Icons.Outlined.AccessAlarm,
        iconTint = PxColors.Primary,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        onSeeAll = onSeeAll,
        headerContent = {
            ProgressRing(
                progress = progress,
                size = 48.dp,
                strokeWidth = 4.dp,
                color = PxColors.Primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        },
        modifier = modifier,
    ) {
        if (tasks.isEmpty()) {
            WidgetEmptyState("No tasks due today")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tasks.take(if (isExpanded) Int.MAX_VALUE else 3).forEach { task ->
                    TaskMiniRow(task = task)
                }
                if (!isExpanded && tasks.size > 3) {
                    TextButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("+${tasks.size - 3} more", color = PxColors.Primary)
                    }
                }
            }
        }
    }
}

@Composable
fun EventsWidget(
    events: List<Event>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WidgetCard(
        title = "Upcoming Events",
        icon = Icons.Outlined.CalendarMonth,
        iconTint = PxColors.Secondary,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        onSeeAll = onSeeAll,
        modifier = modifier,
    ) {
        if (events.isEmpty()) {
            WidgetEmptyState("No upcoming events")
        } else {
            TimelineView(events = events.take(if (isExpanded) Int.MAX_VALUE else 3))
        }
    }
}

@Composable
fun FocusTimeWidget(
    focusMinutes: Int,
    totalMinutes: Int = 120,
    completedSessions: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = (focusMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "focus_progress",
    )

    WidgetCard(
        title = "Focus Time Today",
        icon = Icons.Filled.Timer,
        iconTint = PxColors.Warning,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        onSeeAll = null,
        headerContent = {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = animatedProgress,
                    size = 56.dp,
                    strokeWidth = 5.dp,
                    color = PxColors.Warning,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${focusMinutes}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${focusMinutes}m / ${totalMinutes}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$completedSessions sessions completed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = PxColors.Warning,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
fun RecentNotesWidget(
    notes: List<Note>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WidgetCard(
        title = "Recent Notes",
        icon = Icons.Outlined.StickyNote2,
        iconTint = PxColors.Info,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        onSeeAll = onSeeAll,
        modifier = modifier,
    ) {
        if (notes.isEmpty()) {
            WidgetEmptyState("No recent notes")
        } else if (!isExpanded) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    NoteMiniCard(note = note)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                notes.forEach { note ->
                    NoteExpandedRow(note = note)
                }
            }
        }
    }
}

@Composable
fun AiQuickActionWidget(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WidgetCard(
        title = "AI Assistant",
        icon = Icons.Filled.CheckCircle,
        iconTint = PxColors.PrimaryVariant,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        onSeeAll = null,
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "What can I help you with?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Summarize my day" to { onOpenAi() },
                    "Create a task" to { onOpenAi() },
                ).forEach { (label, onClick) ->
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PxColors.Primary.copy(alpha = 0.1f)),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = PxColors.Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSeeAll: (() -> Unit)?,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onToggleExpand() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (headerContent != null) {
                headerContent()
            }
            if (onSeeAll != null) {
                TextButton(onClick = onSeeAll) {
                    Text("See all", style = MaterialTheme.typography.labelSmall, color = PxColors.Primary)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    trackColor: Color,
) {
    Canvas(modifier = Modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val halfStroke = strokeWidth.toPx() / 2f
        val arcSize = size.toPx() - strokeWidth.toPx()
        val arcTopLeft = Offset(halfStroke, halfStroke)

        drawCircle(
            color = trackColor,
            radius = arcSize / 2f,
            center = Offset(size.toPx() / 2f, size.toPx() / 2f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = Size(arcSize, arcSize),
            style = stroke,
        )
    }
}

@Composable
private fun WidgetEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun TaskMiniRow(task: Task) {
    val priorityColor = when (task.priority.name) {
        "LOW" -> PriorityColors.Low
        "HIGH" -> PriorityColors.High
        "URGENT" -> PriorityColors.Urgent
        else -> PriorityColors.Medium
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (task.status.name == "DONE") Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (task.status.name == "DONE") PxColors.Success else PxColors.Primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(priorityColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = task.priority.name,
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor,
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun TimelineView(events: List<Event>) {
    Column {
        events.forEachIndexed { index, event ->
            val color = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
                .getOrDefault(PxColors.Primary)
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    if (index < events.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(color.copy(alpha = 0.3f)),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "${DateTimeUtils.formatTime(event.startAt)} - ${DateTimeUtils.formatTime(event.endAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteMiniCard(note: Note) {
    Column(
        modifier = Modifier
            .size(width = 150.dp, height = 100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
    ) {
        Text(
            text = note.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = note.preview,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NoteExpandedRow(note: Note) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.StickyNote2,
            contentDescription = null,
            tint = PxColors.Info,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = note.preview,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
