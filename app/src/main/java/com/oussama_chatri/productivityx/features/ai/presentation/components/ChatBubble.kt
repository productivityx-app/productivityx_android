package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import java.time.ZoneId

@Composable
fun ChatBubble(
    message  : Message,
    modifier : Modifier = Modifier,
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isUser) {
            Spacer(Modifier.weight(1f))
            UserBubble(message)
        } else {
            AssistantBubble(message)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun UserBubble(message: Message) {
    Column(horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = Color(0xFF6366F1),
                    shape = RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp),
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text  = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            )
        }
        Text(
            text     = DateTimeUtils.formatTime(message.createdAt, ZoneId.systemDefault()),
            style    = MaterialTheme.typography.labelSmall,
            color    = Color(0xFF888899),
            modifier = Modifier.padding(top = 4.dp, end = 4.dp),
        )
    }
}

@Composable
private fun AssistantBubble(message: Message) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(14.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        Column {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = Color(0xFF1A1A24),
                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Markdown(
                    content    = message.content,
                    colors     = markdownColor(
                        text            = Color(0xFFEEEEF5),
                        linkText        = Color(0xFF6366F1),
                        codeText        = Color(0xFF8B5CF6),
                        codeBackground  = Color(0xFF252533),
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
                text     = DateTimeUtils.formatTime(message.createdAt, ZoneId.systemDefault()),
                style    = MaterialTheme.typography.labelSmall,
                color    = Color(0xFF888899),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}