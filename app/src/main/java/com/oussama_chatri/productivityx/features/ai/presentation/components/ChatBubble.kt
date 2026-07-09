package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import java.time.ZoneId

enum class MessageStatus {
    SENDING,
    SENT,
    READ,
    FAILED,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    isGrouped: Boolean = false,
    status: MessageStatus = MessageStatus.SENT,
    onLongPress: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == MessageRole.USER
    var showActions by remember { mutableStateOf(false) }

    val entryOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(500),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, (entryOffset * 20).toInt()) },
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isUser) {
            Spacer(Modifier.weight(1f))
            UserBubble(
                message = message,
                status = status,
                onLongPress = {
                    showActions = !showActions
                    onLongPress?.invoke()
                },
            )
        } else {
            if (!isGrouped) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PxColors.Primary, PxColors.Secondary),
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(36.dp))
            }
            Column {
                AssistantBubble(
                    message = message,
                    onLongPress = {
                        showActions = !showActions
                        onLongPress?.invoke()
                    },
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }

    AnimatedVisibility(
        visible = showActions,
        enter = fadeIn(animationSpec = tween(200)),
    ) {
        MessageActions(
            message = message,
            onCopy = onCopy,
            onRegenerate = onRegenerate,
            onReply = onReply,
            modifier = Modifier.padding(
                start = if (isUser) 0.dp else 36.dp,
                end = if (isUser) 0.dp else 36.dp,
            ),
        )
    }
}

@Composable
private fun MessageActions(
    message: Message,
    onCopy: (() -> Unit)?,
    onRegenerate: (() -> Unit)?,
    onReply: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = { onCopy?.invoke() },
            modifier = Modifier.background(PxColors.SurfaceVariant, CircleShape),
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        }
        IconButton(
            onClick = { onReply?.invoke() },
            modifier = Modifier.background(PxColors.SurfaceVariant, CircleShape),
        ) {
            Icon(Icons.Outlined.Reply, contentDescription = "Reply", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        }
        if (message.role == MessageRole.ASSISTANT) {
            IconButton(
                onClick = { onRegenerate?.invoke() },
                modifier = Modifier.background(PxColors.SurfaceVariant, CircleShape),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun UserBubble(
    message: Message,
    status: MessageStatus,
    onLongPress: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp))
                .background(PxColors.Primary.copy(alpha = 0.92f))
                .clickable(onClick = onLongPress)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, end = 4.dp),
        ) {
            StatusIndicator(status = status)
            Spacer(Modifier.width(6.dp))
            Text(
                text = DateTimeUtils.formatTime(message.createdAt, ZoneId.systemDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim,
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = PxColors.OnSurfaceDim.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
        MessageStatus.SENT -> {
            Icon(
                Icons.Outlined.DoneAll,
                contentDescription = null,
                tint = PxColors.OnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }
        MessageStatus.READ -> {
            Icon(
                Icons.Outlined.DoneAll,
                contentDescription = null,
                tint = PxColors.Info,
                modifier = Modifier.size(14.dp),
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = PxColors.Error,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun AssistantBubble(
    message: Message,
    onLongPress: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PxColors.Surface,
                            PxColors.Surface.copy(alpha = 0.95f),
                        ),
                    )
                )
                .clickable(onClick = onLongPress)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Markdown(
                content = message.content,
                colors = markdownColor(
                    text = PxColors.OnBackground,
                    linkText = PxColors.Primary,
                    codeText = PxColors.Secondary,
                    codeBackground = PxColors.SurfaceVariant,
                ),
                typography = markdownTypography(
                    text = MaterialTheme.typography.bodyMedium,
                    code = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                ),
            )
        }
        Text(
            text = DateTimeUtils.formatTime(message.createdAt, ZoneId.systemDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
        )
    }
}
