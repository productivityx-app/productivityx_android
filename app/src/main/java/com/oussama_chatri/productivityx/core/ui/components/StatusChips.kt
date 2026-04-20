package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.theme.PriorityColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

// SyncStatusIndicator

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val dotColor = when (syncStatus) {
        SyncStatus.PENDING  -> PxColors.Warning
        SyncStatus.SYNCING  -> PxColors.Info
        SyncStatus.SYNCED   -> PxColors.Success
        SyncStatus.CONFLICT -> PxColors.Error
    }

    val label = when (syncStatus) {
        SyncStatus.PENDING  -> "Pending"
        SyncStatus.SYNCING  -> "Syncing"
        SyncStatus.SYNCED   -> "Synced"
        SyncStatus.CONFLICT -> "Conflict"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "syncAnim")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (syncStatus == SyncStatus.SYNCING || syncStatus == SyncStatus.CONFLICT) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "syncDotAlpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .alpha(if (syncStatus == SyncStatus.SYNCED) 1f else alpha)
                .background(dotColor)
        )
        if (showLabel) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
            )
        }
    }
}

// PriorityChip

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.LOW    -> PriorityColors.Low
        Priority.MEDIUM -> PriorityColors.Medium
        Priority.HIGH   -> PriorityColors.High
        Priority.URGENT -> PriorityColors.Urgent
    }
    val label = priority.name.lowercase().replaceFirstChar { it.uppercaseChar() }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// StatusChip

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        TaskStatus.TODO        -> PxColors.OnSurfaceDim to "To Do"
        TaskStatus.IN_PROGRESS -> PxColors.Info         to "In Progress"
        TaskStatus.ON_HOLD     -> PxColors.Warning      to "On Hold"
        TaskStatus.DONE        -> PxColors.Success      to "Done"
        TaskStatus.CANCELLED   -> PxColors.Error        to "Cancelled"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun ChipsPreview() {
    ProductivityXTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            PriorityChip(Priority.LOW)
            PriorityChip(Priority.MEDIUM)
            PriorityChip(Priority.HIGH)
            PriorityChip(Priority.URGENT)
        }
    }
}