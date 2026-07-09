package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock

@Composable
fun ActionCard(
    action: AiActionBlock,
    onConfirm: (AiActionBlock) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, label, description) = action.toDisplayInfo()
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(200))
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .clickable { isExpanded = !isExpanded },
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 4.dp),
        ) {
            Spacer(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(PxColors.Primary)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PxColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = PxColors.Primary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = PxColors.OnSurfaceDim,
                            modifier = Modifier.size(16.dp).rotate(rotation),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnBackground,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurfaceDim,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row {
                    FilledTonalButton(onClick = { onConfirm(action) }) {
                        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ai_action_confirm), style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.width(8.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.ai_action_dismiss),
                            style = MaterialTheme.typography.labelMedium,
                            color = PxColors.OnSurfaceDim,
                        )
                    }
                }
            }
        }
    }
}

private data class ActionDisplayInfo(
    val icon: ImageVector,
    val label: String,
    val description: String,
)

@Composable
private fun AiActionBlock.toDisplayInfo(): ActionDisplayInfo = when (this) {
    is AiActionBlock.CreateTask -> ActionDisplayInfo(
        icon = Icons.Outlined.CheckCircle,
        label = stringResource(R.string.ai_action_create_task),
        description = buildString {
            append(title)
            priority?.let { append(" \u00B7 ${it.name}") }
            dueDate?.let { append(" \u00B7 Due $it") }
        },
    )
    is AiActionBlock.CreateNote -> ActionDisplayInfo(
        icon = Icons.Outlined.StickyNote2,
        label = stringResource(R.string.ai_action_create_note),
        description = title,
    )
    is AiActionBlock.AddEvent -> ActionDisplayInfo(
        icon = Icons.Outlined.CalendarMonth,
        label = stringResource(R.string.ai_action_add_event),
        description = buildString {
            append(title)
            durationMinutes?.let { append(" \u00B7 ${it}min") }
        },
    )
}
