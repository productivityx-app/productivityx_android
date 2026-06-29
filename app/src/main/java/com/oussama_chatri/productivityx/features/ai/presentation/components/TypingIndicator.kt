package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val dotCount = 3
    val scales = List(dotCount) { remember { Animatable(1f) } }

    scales.forEachIndexed { index, scale ->
        LaunchedEffect(scale) {
            delay(index * 150L)
            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            )
        }
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(PxColors.Primary, PxColors.Secondary),
                        ),
                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    scales.forEach { scale ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(scale.value)
                                .background(PxColors.OnPrimary, CircleShape)
                        )
                    }
                }
            }
            if (showLabel) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI is thinking",
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim,
                )
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Processing your request...",
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
