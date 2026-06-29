package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@Composable
fun StreamingBubble(
    content: String,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
    )

    Row(
        modifier = modifier.alpha(alpha),
        verticalAlignment = Alignment.Bottom,
    ) {
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

        if (content.isEmpty()) {
            TypingIndicator(showLabel = false)
        } else {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PxColors.Surface,
                                PxColors.Surface.copy(alpha = 0.95f),
                            ),
                        ),
                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnBackground,
                )
            }
        }
    }
}
