package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = composed {
    val shimmerColors = listOf(
        PxColors.SurfaceVariant.copy(alpha = 0.6f),
        PxColors.SurfaceVariant.copy(alpha = 0.2f),
        PxColors.SurfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate_animation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )

    this.background(brush = brush, shape = shape)
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier.shimmerEffect(shape)
    )
}

@Composable
fun ListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
    itemHeight: androidx.compose.ui.unit.Dp = 72.dp
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.androidx.compose.foundation.layout.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(itemCount) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .androidx.compose.foundation.layout.fillMaxWidth()
                    .androidx.compose.foundation.layout.height(itemHeight)
            )
        }
    }
}
