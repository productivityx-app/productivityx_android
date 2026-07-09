package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.rememberDebouncedClick
import com.oussama_chatri.productivityx.features.ai.domain.model.Message

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isStreaming: Boolean,
    replyToMessage: Message? = null,
    onCancelReply: (() -> Unit)? = null,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val canSend = value.isNotBlank() && !isStreaming

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut(),
        ) {
                @OptIn(ExperimentalLayoutApi::class) FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PxColors.Surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        onClick = { onSuggestionClick?.invoke(suggestion) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = replyToMessage != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut(),
        ) {
            replyToMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PxColors.SurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Reply,
                        contentDescription = "Send message",
                        tint = PxColors.Primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (msg.content.length > 60) msg.content.take(60) + "..." else msg.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurface,
                            maxLines = 1,
                        )
                    }
                    IconButton(onClick = { onCancelReply?.invoke() }) {
                        Icon(Icons.Outlined.Close, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        HorizontalDivider(color = PxColors.SurfaceVariant)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(PxColors.SurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PxColors.Surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ai_message_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnSurfaceDim,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = PxColors.OnBackground),
                    cursorBrush = SolidColor(PxColors.Primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() }
                    ),
                )
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = rememberDebouncedClick(onClick = onSend),
                enabled = canSend,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canSend) PxColors.Primary else PxColors.SurfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = stringResource(R.string.cd_send_message),
                    tint = if (canSend) Color.White else PxColors.OnSurfaceDim,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PxColors.SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurface,
        )
    }
}
