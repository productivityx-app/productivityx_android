package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val dotCount  = 3
    val scales    = List(dotCount) { remember { Animatable(1f) } }

    scales.forEachIndexed { index, scale ->
        LaunchedEffect(scale) {
            delay(index * 150L)
            scale.animateTo(
                targetValue    = 1.4f,
                animationSpec  = infiniteRepeatable(
                    animation  = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            )
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF1A1A24), RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            scales.forEach { scale ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(scale.value)
                        .background(Color(0xFF6366F1), CircleShape)
                )
            }
        }
    }
}
