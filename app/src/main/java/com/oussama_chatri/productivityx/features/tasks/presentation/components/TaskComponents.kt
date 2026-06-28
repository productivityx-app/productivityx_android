package com.oussama_chatri.productivityx.features.tasks.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.state.displayLabel

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
            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
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

// ─── Status Chip ─────────────────────────────────────────────────────────────

@Composable
fun StatusChip(
    status: TaskStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF6366F1) else Color(0xFF252533),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statusChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF888899),
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

// ─── Task Checkbox ────────────────────────────────────────────────────────────

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

    val checkboxDescription = if (checked) stringResource(R.string.cd_uncheck_task) else stringResource(R.string.cd_check_task)

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
            .semantics {
                contentDescription = checkboxDescription
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Task List Item ───────────────────────────────────────────────────────────

@Composable
fun TaskListItem(
    task: Task,
    onTaskClick: (String) -> Unit,
    onCheckChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.DONE
    val titleColor by animateColorAsState(
        targetValue = if (isDone) Color(0xFF888899) else Color(0xFFEEEEF5),
        label = "titleColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTaskClick(task.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskCheckbox(
            checked = isDone,
            priority = task.priority,
            onClick = { onCheckChange(task.id, !isDone) }
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            if (task.dueDate != null || task.hasSubtasks) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.dueDate != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = if (task.isOverdue) Color(0xFFEF4444) else Color(0xFF888899),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = task.dueDate.toString(),
                                color = if (task.isOverdue) Color(0xFFEF4444) else Color(0xFF888899),
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (task.hasSubtasks) {
                        Text(
                            text = "${task.completedSubtaskCount}/${task.subtaskCount}",
                            color = Color(0xFF888899),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        PriorityChip(priority = task.priority)

        if (task.hasSubtasks) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF888899),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Kanban Card ─────────────────────────────────────────────────────────────

@Composable
fun KanbanTaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF252533),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                    color = Color(0xFFEEEEF5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
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
                        tint = if (task.isOverdue) Color(0xFFEF4444) else Color(0xFF888899),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = task.dueDate.toString(),
                        color = if (task.isOverdue) Color(0xFFEF4444) else Color(0xFF888899),
                        fontSize = 11.sp
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
            color = Color(0xFFCCCCD8),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF6366F1))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
            color = if (isDone) Color(0xFF888899) else Color(0xFFCCCCD8),
            fontSize = 13.sp,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
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
            },
            modifier = Modifier.size(32.dp)
        ) {
            Text("−", color = Color(0xFF6366F1), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = if (value != null) "$value min" else "—",
            color = Color(0xFFCCCCD8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        IconButton(
            onClick = { onValueChange((value ?: 20) + 5) },
            modifier = Modifier.size(32.dp)
        ) {
            Text("+", color = Color(0xFF6366F1), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Setting Row (used in AddEditTask sheet) ──────────────────────────────────

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
            color = Color(0xFFCCCCD8),
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}
