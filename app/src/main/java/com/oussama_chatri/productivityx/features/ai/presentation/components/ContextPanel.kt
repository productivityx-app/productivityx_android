package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.AiContext

@Composable
fun ContextPanel(
    context          : AiContext?,
    isLoading        : Boolean,
    onRefresh        : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A24), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Text(
                text     = "Your Workspace",
                style    = MaterialTheme.typography.titleMedium,
                color    = Color(0xFFEEEEF5),
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh context",
                    modifier    = Modifier.size(14.dp),
                    tint        = Color(0xFF6366F1),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = if (isLoading) "Refreshing..." else "Refresh",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6366F1),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (context != null) {
            ContextRow(
                icon  = Icons.Outlined.CheckCircle,
                label = "Active tasks",
                value = "${context.totalActiveTasks} (${context.tasksOverdue} overdue)",
            )
            ContextRow(
                icon  = Icons.Outlined.CalendarMonth,
                label = "Events this week",
                value = "${context.upcomingEventsThisWeek}",
            )
            ContextRow(
                icon  = Icons.Outlined.StickyNote2,
                label = "Last note",
                value = context.lastEditedNoteTitle ?: "—",
            )
            ContextRow(
                icon  = Icons.Outlined.Timer,
                label = "Focus today",
                value = DateTimeUtils.focusDurationLabel(context.todayFocusMinutes),
            )
        } else {
            Text(
                text  = "Workspace context unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888899),
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF252533))
        Spacer(Modifier.height(8.dp))

        Text(
            text  = "This context is sent with every message.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888899),
        )
    }
}

@Composable
private fun ContextRow(
    icon  : ImageVector,
    label : String,
    value : String,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color(0xFF888899), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = Color(0xFF888899),
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCCCCD8),
        )
    }
}
