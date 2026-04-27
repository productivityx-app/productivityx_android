package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp

/**
 * Rendered in place of the final message bubble while the SSE stream is active.
 * Switches to TypingIndicator when content is still empty (latency before first token).
 */
@Composable
fun StreamingBubble(
    content  : String,
    modifier : Modifier = Modifier,
) {
    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier         = Modifier
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

        if (content.isEmpty()) {
            TypingIndicator()
        } else {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = Color(0xFF1A1A24),
                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text  = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEEEEF5),
                )
            }
        }
    }
}
