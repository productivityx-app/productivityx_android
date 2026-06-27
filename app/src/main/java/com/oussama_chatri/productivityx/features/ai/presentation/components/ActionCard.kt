package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock

@Composable
fun ActionCard(
    action    : AiActionBlock,
    onConfirm : (AiActionBlock) -> Unit,
    onDismiss : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    val (icon, label, description) = action.toDisplayInfo()

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A24)),
    ) {
        // Primary-colored accent bar
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Color(0xFF6366F1))
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color(0xFF6366F1),
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6366F1),
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFEEEEF5),
            )

            Spacer(Modifier.height(12.dp))

            Row {
                FilledTonalButton(
                    onClick = { onConfirm(action) },
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.ai_action_confirm), style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text  = stringResource(R.string.ai_action_dismiss),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF888899),
                    )
                }
            }
        }
    }
}

private data class ActionDisplayInfo(
    val icon        : ImageVector,
    val label       : String,
    val description : String,
)

@Composable
private fun AiActionBlock.toDisplayInfo(): ActionDisplayInfo = when (this) {
    is AiActionBlock.CreateTask -> ActionDisplayInfo(
        icon        = Icons.Outlined.CheckCircle,
        label       = stringResource(R.string.ai_action_create_task),
        description = buildString {
            append(title)
            priority?.let { append(" · ${it.name}") }
            dueDate?.let  { append(" · Due $it") }
        },
    )
    is AiActionBlock.CreateNote -> ActionDisplayInfo(
        icon        = Icons.Outlined.StickyNote2,
        label       = stringResource(R.string.ai_action_create_note),
        description = title,
    )
    is AiActionBlock.AddEvent   -> ActionDisplayInfo(
        icon        = Icons.Outlined.CalendarMonth,
        label       = stringResource(R.string.ai_action_add_event),
        description = buildString {
            append(title)
            durationMinutes?.let { append(" · ${it}min") }
        },
    )
}
