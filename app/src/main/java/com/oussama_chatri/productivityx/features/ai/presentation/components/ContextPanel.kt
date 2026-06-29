package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.AiContext
import com.oussama_chatri.productivityx.features.ai.presentation.state.AiPersonaType

@Composable
fun ContextPanel(
    context: AiContext?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    personaType: AiPersonaType = AiPersonaType.PRODUCTIVITY,
    onPersonaChange: ((AiPersonaType) -> Unit)? = null,
    isExpanded: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .background(PxColors.Surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.ai_context_title),
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnBackground,
                modifier = Modifier.weight(1f),
            )

            PersonaAvatar(personaType = personaType, size = 24)

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.ai_context_refresh),
                    modifier = Modifier.size(14.dp),
                    tint = PxColors.Primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isLoading) stringResource(R.string.refreshing) else stringResource(R.string.ai_context_refresh),
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.Primary,
                )
            }

            Spacer(Modifier.width(4.dp))

            TextButton(onClick = { onToggleExpanded?.invoke() }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = PxColors.OnSurfaceDim,
                    modifier = Modifier.size(18.dp).rotate(rotation),
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp))

                if (context != null) {
                    ContextRow(
                        icon = Icons.Outlined.CheckCircle,
                        label = stringResource(R.string.ai_context_active_tasks),
                        value = "${context.totalActiveTasks} (${context.tasksOverdue} ${stringResource(R.string.ai_context_overdue)})",
                    )
                    ContextRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = stringResource(R.string.ai_context_events_week),
                        value = "${context.upcomingEventsThisWeek}",
                    )
                    ContextRow(
                        icon = Icons.Outlined.StickyNote2,
                        label = stringResource(R.string.ai_context_last_note),
                        value = context.lastEditedNoteTitle ?: "\u2014",
                    )
                    ContextRow(
                        icon = Icons.Outlined.Timer,
                        label = stringResource(R.string.ai_context_focus_today),
                        value = DateTimeUtils.focusDurationLabel(context.todayFocusMinutes),
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = PxColors.SurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.ai_context_caption),
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim,
                    )

                    if (onPersonaChange != null) {
                        Spacer(Modifier.height(12.dp))
                        PersonaSelector(
                            current = personaType,
                            onSelect = onPersonaChange,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.ai_context_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim,
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = PxColors.SurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ai_context_caption),
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonaAvatar(
    personaType: AiPersonaType,
    size: Int = 24,
) {
    val (icon, colors) = when (personaType) {
        AiPersonaType.PRODUCTIVITY -> Icons.Outlined.Bolt to listOf(PxColors.Primary, PxColors.PrimaryVariant)
        AiPersonaType.CREATIVE -> Icons.Outlined.AutoAwesome to listOf(PxColors.Secondary, PxColors.Primary)
        AiPersonaType.TECHNICAL -> Icons.Outlined.Code to listOf(PxColors.Info, PxColors.Primary)
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size / 2).dp),
        )
    }
}

@Composable
private fun PersonaSelector(
    current: AiPersonaType,
    onSelect: (AiPersonaType) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "AI Persona",
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim,
        )
        Spacer(Modifier.width(8.dp))
        AiPersonaType.entries.forEach { type ->
            val isSelected = type == current
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PxColors.Primary.copy(alpha = 0.15f) else PxColors.SurfaceVariant)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonaAvatar(personaType = type, size = 16)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurfaceDim,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurface,
        )
    }
}
