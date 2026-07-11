package com.oussama_chatri.productivityx.features.tasks.presentation.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import java.time.LocalDate
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel
import kotlin.math.roundToInt

// ─── Priority Indicator (color bar) ─────────────────────────────────────────────

@Composable
fun PriorityIndicator(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
            .background(priorityAccentColor(priority))
    )
}

// ─── Priority Chip ────────────────────────────────────────────────────────────

@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = priorityColors(priority)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = priority.displayLabel,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

val Priority.displayLabel: String get() = when (this) {
    Priority.LOW -> "Low"
    Priority.MEDIUM -> "Medium"
    Priority.HIGH -> "High"
    Priority.URGENT -> "Urgent"
}

@Composable
fun priorityColors(priority: Priority): Pair<Color, Color> = when (priority) {
    Priority.LOW -> Color(0xFF6B7280).copy(alpha = 0.15f) to Color(0xFF9CA3AF)
    Priority.MEDIUM -> Color(0xFF3B82F6).copy(alpha = 0.15f) to Color(0xFF60A5FA)
    Priority.HIGH -> Color(0xFFF59E0B).copy(alpha = 0.15f) to Color(0xFFFBBF24)
    Priority.URGENT -> Color(0xFFEF4444).copy(alpha = 0.15f) to Color(0xFFF87171)
}

fun priorityAccentColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> Color(0xFF6B7280)
    Priority.MEDIUM -> Color(0xFF3B82F6)
    Priority.HIGH -> Color(0xFFF59E0B)
    Priority.URGENT -> Color(0xFFEF4444)
}

fun dueDateUrgencyColor(task: Task): Color = when {
    task.status == TaskStatus.DONE -> PxColors.Success
    task.isOverdue -> PxColors.Error
    task.isDueToday -> PxColors.Warning
    task.dueDate != null -> PxColors.OnSurfaceDim
    else -> PxColors.OnSurfaceDim
}

// ─── Status Chip ─────────────────────────────────────────────────────────────

@Composable
fun StatusChip(
    status: TaskStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) PxColors.Primary else PxColors.SurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statusChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) PxColors.OnPrimary else PxColors.OnSurfaceDim,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statusChipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = status.displayLabel,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─── Animated Task Checkbox ──────────────────────────────────────────────────

@Composable
fun TaskCheckbox(
    checked: Boolean,
    priority: Priority,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = priorityAccentColor(priority)
    val checkboxColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkboxColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkboxScale"
    )

    val description = if (checked) stringResource(R.string.cd_uncheck_task) else stringResource(R.string.cd_check_task)

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = if (checked) 0.dp else 2.dp,
                color = if (checked) Color.Transparent else accentColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp)
            )
            .background(checkboxColor)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn(tween(200)) + fadeIn(tween(200))
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = PxColors.OnPrimary,
                modifier = Modifier
                    .size(16.dp)
                    .scale(scale)
            )
        }
    }
}

// ─── Tag Chip ─────────────────────────────────────────────────────────────────

@Composable
fun TagChip(
    tag: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(PxColors.SurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = tag,
            color = PxColors.OnSurfaceDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Assignee Avatar ─────────────────────────────────────────────────────────

@Composable
fun AssigneeAvatar(
    name: String?,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val initial = name?.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(PxColors.Primary)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            // Placeholder for Coil async image loading
            Text(initial, color = PxColors.OnPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(initial, color = PxColors.OnPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Subtask Progress Indicator ──────────────────────────────────────────────

@Composable
fun SubtaskProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$completed/$total",
                color = PxColors.OnSurfaceDim,
                fontSize = 10.sp
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = PxColors.Primary,
            trackColor = PxColors.SurfaceVariant
        )
    }
}

// ─── Completion Celebration ──────────────────────────────────────────────────

@Composable
fun CompletionCelebration(
    isMilestone: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAnimation by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "celebrationScale"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val strokeWidth = 6.dp.toPx()
            drawCircle(
                color = PxColors.Primary.copy(alpha = 0.2f),
                radius = size.minDimension / 2
            )
            drawArc(
                color = PxColors.Primary,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.task_completed),
            tint = PxColors.Success,
            modifier = Modifier.size(32.dp)
        )
    }
}

// ─── Drag Handle ─────────────────────────────────────────────────────────────

@Composable
fun DragHandle(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(PxColors.OnSurfaceDim)
            )
        }
    }
}

// ─── Recurrence Badge ────────────────────────────────────────────────────────

@Composable
fun RecurrenceBadge(
    task: Task,
    modifier: Modifier = Modifier
) {
    if (task.isRecurring) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = stringResource(R.string.cd_recurring),
                tint = PxColors.OnSurfaceDim,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = task.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() },
                color = PxColors.OnSurfaceDim,
                fontSize = 10.sp
            )
        }
    }
}

// ─── Enhanced Task List Item ─────────────────────────────────────────────────

@Composable
fun TaskListItem(
    task: Task,
    onTaskClick: (String) -> Unit,
    onCheckChange: (String, Boolean) -> Unit,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.DONE
    val titleColor by animateColorAsState(
        targetValue = if (isDone) PxColors.OnSurfaceDim else PxColors.OnBackground,
        label = "titleColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        label = "selectedBg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onTaskClick(task.id) }
            .padding(start = 0.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority color bar
        PriorityIndicator(
            priority = task.priority,
            modifier = Modifier
                .height(if (task.hasSubtasks) 72.dp else 56.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        TaskCheckbox(
            checked = isDone,
            priority = task.priority,
            onClick = { onCheckChange(task.id, !isDone) }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = task.title,
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (task.isRecurring) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = PxColors.OnSurfaceDim,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date
                if (task.dueDate != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = dueDateUrgencyColor(task),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatDueDate(task),
                            color = dueDateUrgencyColor(task),
                            fontSize = 12.sp
                        )
                    }
                }

                // Tags
                task.tags.take(2).forEach { tag ->
                    TagChip(tag = tag)
                }
                if (task.tags.size > 2) {
                    Text(
                        text = "+${task.tags.size - 2}",
                        color = PxColors.OnSurfaceDim,
                        fontSize = 10.sp
                    )
                }

                // Assignee
                if (task.assigneeId != null) {
                    AssigneeAvatar(
                        name = task.assigneeName,
                        avatarUrl = task.assigneeAvatar
                    )
                }
            }

            // Subtask progress
            if (task.hasSubtasks) {
                Spacer(modifier = Modifier.height(6.dp))
                SubtaskProgressBar(
                    completed = task.completedSubtaskCount,
                    total = task.subtaskCount
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        PriorityChip(priority = task.priority)
    }
}

@Composable
private fun formatDueDate(task: Task): String {
    val date = task.dueDate ?: return ""
    val overdueLabel = stringResource(R.string.task_overdue)
    val todayLabel = stringResource(R.string.today)
    val tomorrowLabel = stringResource(R.string.tomorrow)
    return when {
        task.isOverdue -> overdueLabel
        task.isDueToday -> todayLabel
        date == LocalDate.now().plusDays(1) -> tomorrowLabel
        else -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} ${date.dayOfMonth}"
    }
}

// Need to import LocalDate for formatDueDate
// ─── Kanban Task Card (Enhanced) ────────────────────────────────────────────

@Composable
fun KanbanTaskCard(
    task: Task,
    onClick: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 8f else 0f,
        label = "cardElevation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .graphicsLayer {
                shadowElevation = elevation
                translationX = if (isDragging) 4.dp.toPx() else 0f
                translationY = if (isDragging) (-4).dp.toPx() else 0f
            }
            .clickable(onClick = onClick),
        color = PxColors.SurfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(start = 0.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)) {
            // Priority bar
            PriorityIndicator(
                priority = task.priority,
                modifier = Modifier
                    .height(3.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(priorityAccentColor(task.priority))
                )
                Text(
                    text = task.title,
                    color = PxColors.OnBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tags
            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    task.tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            if (task.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = dueDateUrgencyColor(task),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = formatDueDate(task),
                        color = dueDateUrgencyColor(task),
                        fontSize = 11.sp
                    )
                }
            }

            // Subtask progress
            if (task.hasSubtasks) {
                Spacer(modifier = Modifier.height(6.dp))
                SubtaskProgressBar(
                    completed = task.completedSubtaskCount,
                    total = task.subtaskCount
                )
            }

            // Assignee
            if (task.assigneeId != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssigneeAvatar(
                        name = task.assigneeName,
                        avatarUrl = task.assigneeAvatar,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = task.assigneeName ?: stringResource(R.string.task_assigned),
                        color = PxColors.OnSurfaceDim,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ─── Kanban Column Header ─────────────────────────────────────────────────────

@Composable
fun KanbanColumnHeader(
    status: TaskStatus,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = status.displayLabel,
            color = PxColors.OnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(PxColors.Primary)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = PxColors.OnPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Drop Zone ───────────────────────────────────────────────────────────────

@Composable
fun DropZone(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) PxColors.Primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "dropZoneBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) PxColors.Primary else PxColors.Outline,
        label = "dropZoneBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) PxColors.Primary else PxColors.OnSurfaceDim,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Subtask Row ──────────────────────────────────────────────────────────────

@Composable
fun SubtaskRow(
    task: Task,
    onCheckChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.DONE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskCheckbox(
            checked = isDone,
            priority = task.priority,
            onClick = { onCheckChange(task.id, !isDone) },
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = task.title,
            color = if (isDone) PxColors.OnSurfaceDim else PxColors.OnSurface,
            fontSize = 13.sp,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (task.tags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                task.tags.take(1).forEach { tag ->
                    TagChip(tag = tag, modifier = Modifier.padding(0.dp))
                }
            }
        }
    }
}

// ─── Estimated Time Stepper ───────────────────────────────────────────────────

@Composable
fun MinuteStepper(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = {
                val current = value ?: 25
                if (current > 5) onValueChange(current - 5)
            }
        ) {
            Text("\u2212", color = PxColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = if (value != null) stringResource(R.string.tasks_minutes, value) else "\u2014",
            color = PxColors.OnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        IconButton(
            onClick = { onValueChange((value ?: 20) + 5) }
        ) {
            Text("+", color = PxColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Setting Row ──────────────────────────────────────────────────

@Composable
fun TaskSettingRow(
    icon: @Composable () -> Unit,
    label: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(24.dp)) { icon() }
        Text(
            text = label,
            color = PxColors.OnSurface,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

// ─── Calendar Day Cell ────────────────────────────────────────────────────────

@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    taskCount: Int,
    hasTasks: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> PxColors.Primary
            isToday -> PxColors.Primary.copy(alpha = 0.2f)
            else -> Color.Transparent
        },
        label = "dayCellBg"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                color = when {
                    isSelected -> PxColors.OnPrimary
                    isToday -> PxColors.Primary
                    else -> PxColors.OnSurface
                },
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PxColors.OnPrimary else PxColors.Primary)
                )
            }
        }
    }
}

// ─── Timeline Task Bar ────────────────────────────────────────────────────────

@Composable
fun TimelineTaskBar(
    task: Task,
    startOffset: Float,
    widthFraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = (startOffset * 200).dp)
            .width((widthFraction * 200).dp.coerceAtLeast(20.dp))
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(priorityAccentColor(task.priority).copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(priorityAccentColor(task.priority))
            )
            Text(
                text = task.title,
                color = PxColors.OnBackground,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── Empty State (per view) ───────────────────────────────────────────────────

@Composable
fun TaskEmptyState(
    viewMode: com.oussama_chatri.productivityx.core.enums.TaskView,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, title, subtitle) = when (viewMode) {
        com.oussama_chatri.productivityx.core.enums.TaskView.LIST -> Triple(
            Icons.Outlined.CheckCircle,
            stringResource(R.string.empty_tasks_list),
            stringResource(R.string.empty_tasks_list_hint)
        )
        com.oussama_chatri.productivityx.core.enums.TaskView.KANBAN -> Triple(
            Icons.Outlined.CheckCircle,
            stringResource(R.string.empty_tasks_kanban),
            stringResource(R.string.empty_tasks_kanban_hint)
        )
        com.oussama_chatri.productivityx.core.enums.TaskView.CALENDAR -> Triple(
            Icons.Outlined.CalendarMonth,
            stringResource(R.string.empty_tasks_calendar),
            stringResource(R.string.empty_tasks_calendar_hint)
        )
        com.oussama_chatri.productivityx.core.enums.TaskView.TIMELINE -> Triple(
            Icons.Outlined.AccessTime,
            stringResource(R.string.empty_tasks_timeline),
            stringResource(R.string.empty_tasks_timeline_hint)
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PxColors.OnSurfaceDim,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                color = PxColors.OnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = PxColors.OnSurfaceDim,
                fontSize = 13.sp
            )
        }
    }
}

// ─── Smart Filter Chip ────────────────────────────────────────────────────────

@Composable
fun SmartFilterChip(
    label: String,
    isSelected: Boolean,
    count: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary else PxColors.SurfaceVariant,
        label = "filterChipBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isSelected) PxColors.OnPrimary else PxColors.OnSurfaceDim,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    color = if (isSelected) PxColors.OnPrimary.copy(alpha = 0.7f) else PxColors.OnSurfaceDim,
                    fontSize = 11.sp
                )
            }
        }
    }
}
