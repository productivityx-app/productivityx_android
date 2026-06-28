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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
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
            .background(PxColors.Surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Text(
                text     = stringResource(R.string.ai_context_title),
                style    = MaterialTheme.typography.titleMedium,
                color    = PxColors.OnBackground,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.ai_context_refresh),
                    modifier    = Modifier.size(14.dp),
                    tint        = PxColors.Primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = if (isLoading) stringResource(R.string.refreshing) else stringResource(R.string.ai_context_refresh),
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.Primary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (context != null) {
            ContextRow(
                icon  = Icons.Outlined.CheckCircle,
                label = stringResource(R.string.ai_context_active_tasks),
                value = "${context.totalActiveTasks} (${context.tasksOverdue} ${stringResource(R.string.ai_context_overdue)})",
            )
            ContextRow(
                icon  = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.ai_context_events_week),
                value = "${context.upcomingEventsThisWeek}",
            )
            ContextRow(
                icon  = Icons.Outlined.StickyNote2,
                label = stringResource(R.string.ai_context_last_note),
                value = context.lastEditedNoteTitle ?: "—",
            )
            ContextRow(
                icon  = Icons.Outlined.Timer,
                label = stringResource(R.string.ai_context_focus_today),
                value = DateTimeUtils.focusDurationLabel(context.todayFocusMinutes),
            )
        } else {
            Text(
                text  = stringResource(R.string.ai_context_caption),
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = PxColors.SurfaceVariant)
        Spacer(Modifier.height(8.dp))

        Text(
            text  = stringResource(R.string.ai_context_caption),
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim,
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
        Icon(icon, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = PxColors.OnSurfaceDim,
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurface,
        )
    }
}
