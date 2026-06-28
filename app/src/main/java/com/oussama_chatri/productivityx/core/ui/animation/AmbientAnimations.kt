package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FloatingShapeConfig(
    val count: Int = 8,
    val minSize: Dp = 4.dp,
    val maxSize: Dp = 16.dp,
    val opacity: Float = 0.04f,
    val speed: Float = 1f,
    val colors: List<Color> = listOf(
        Color(0xFF6366F1).copy(alpha = 0.1f),
        Color(0xFF8B5CF6).copy(alpha = 0.08f),
        Color(0xFFEC4899).copy(alpha = 0.06f),
    ),
)

@Composable
fun FloatingShapes(
    config: FloatingShapeConfig = FloatingShapeConfig(),
    modifier: Modifier = Modifier,
) {
    val shapes = remember {
        List(config.count) {
            FloatingShape(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * (config.maxSize - config.minSize).value + config.minSize.value,
                speedX = (Random.nextFloat() - 0.5f) * config.speed * 0.3f,
                speedY = (Random.nextFloat() - 0.5f) * config.speed * 0.3f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 30f,
                color = config.colors[Random.nextInt(config.colors.size)],
                shapeType = ShapeType.entries[Random.nextInt(ShapeType.entries.size)],
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "floatingShapes")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    val density = LocalDensity.current

    Canvas(modifier = modifier.fillMaxSize()) {
        shapes.forEach { shape ->
            drawFloatingShape(
                shape = shape,
                time = time,
                canvasWidth = size.width,
                canvasHeight = size.height,
                density = density.density,
            )
        }
    }
}

private enum class ShapeType { CIRCLE, SQUARE, TRIANGLE, DIAMOND }

private data class FloatingShape(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotationSpeed: Float,
    val color: Color,
    val shapeType: ShapeType,
)

private fun DrawScope.drawFloatingShape(
    shape: FloatingShape,
    time: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    density: Float,
) {
    val offsetX = (shape.x * canvasWidth + sin(time * 0.005f + shape.x * 10f) * 20f)
        .mod(canvasWidth + 100f) - 50f
    val offsetY = (shape.y * canvasHeight + cos(time * 0.004f + shape.y * 10f) * 20f)
        .mod(canvasHeight + 100f) - 50f
    val rotation = (time * 0.01f * shape.rotationSpeed).mod(360f)
    val sizePx = shape.size * density

    drawCircle(
        color = shape.color,
        radius = sizePx / 2f,
        center = Offset(offsetX, offsetY),
    )
}

@Composable
fun GradientMeshBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF6366F1).copy(alpha = 0.05f),
        Color(0xFF8B5CF6).copy(alpha = 0.03f),
        Color(0xFFEC4899).copy(alpha = 0.02f),
    ),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientMesh")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "meshOffset",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val startX = size.width * (0.2f + offset * 0.6f)
        val startY = size.height * (0.2f + offset * 0.6f)
        val endX = size.width * (0.3f - offset * 0.2f)
        val endY = size.height * (0.7f + offset * 0.3f)

        drawRect(
            brush = Brush.horizontalGradient(
                colors = colors,
                startX = startX,
                endX = startX + size.width * 0.5f,
            ),
            size = size,
        )
    }
}

@Composable
fun SuccessConfetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
) {
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.6f,
                startY = -0.1f,
                endY = 1.2f,
                size = Random.nextFloat() * 8.dp.value + 4.dp.value,
                color = Color(
                    red = Random.nextFloat().coerceIn(0.3f, 1f),
                    green = Random.nextFloat().coerceIn(0.3f, 1f),
                    blue = Random.nextFloat().coerceIn(0.3f, 1f),
                    alpha = 0.8f,
                ),
                delay = Random.nextFloat() * 0.5f,
                duration = Random.nextFloat() * 0.5f + 0.5f,
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val progress = 1f
            if (progress <= 1f) {
                val x = particle.x * size.width
                val y = (particle.startY + (particle.endY - particle.startY) * progress) * size.height
                val alpha = (1f - progress).coerceIn(0f, 1f)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size.toFloat(),
                    center = Offset(x, y),
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val endY: Float,
    val size: Float,
    val color: Color,
    val delay: Float,
    val duration: Float,
)

@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = Color(0xFF6366F1),
    trackColor: Color = Color(0xFF2A2A3E),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressRing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Canvas(modifier = modifier.size(size)) {
        val sweepAngle = progress * 360f

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )

        drawArc(
            color = color,
            startAngle = -90f + rotation,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }
}

@Composable
fun AiBreathingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    color: Color = Color(0xFF6366F1),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiBreathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingScale",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingAlpha",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha,
            )
            .background(color, shape = androidx.compose.foundation.shape.CircleShape),
    )
}

@Composable
fun AmbientAnimation(
    modifier: Modifier = Modifier,
    showGradient: Boolean = true,
    showShapes: Boolean = true,
) {
    Box(modifier = modifier) {
        if (showShapes) {
            FloatingShapes(
                config = FloatingShapeConfig(count = 6),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
