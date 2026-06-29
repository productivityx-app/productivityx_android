package com.oussama_chatri.productivityx.features.auth.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.auth.presentation.state.SplashUiState
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

private data class SplashTip(val text: String)

private val splashTips = listOf(
    SplashTip("Stay organized with smart folders"),
    SplashTip("Focus with the Pomodoro timer"),
    SplashTip("AI-powered task suggestions"),
    SplashTip("Sync across all your devices"),
    SplashTip("Never miss a deadline again"),
)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val contentAlpha = remember { Animatable(0f) }
    val routingAlpha = remember { Animatable(1f) }
    var shouldRoute by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState) {
        when (uiState) {
            SplashUiState.Checking -> {
                contentAlpha.animateTo(1f, animationSpec = tween(600))
            }
            SplashUiState.Authenticated -> {
                routingAlpha.animateTo(0f, animationSpec = tween(300))
                shouldRoute = 1
            }
            SplashUiState.ShowOnboarding -> {
                routingAlpha.animateTo(0f, animationSpec = tween(300))
                shouldRoute = 2
            }
            SplashUiState.ShowLogin -> {
                routingAlpha.animateTo(0f, animationSpec = tween(300))
                shouldRoute = 3
            }
        }
    }

    LaunchedEffect(shouldRoute) {
        if (shouldRoute == 1) onNavigateToHome()
        else if (shouldRoute == 2) onNavigateToOnboarding()
        else if (shouldRoute == 3) onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = routingAlpha.value)
            .background(PxColors.Background),
        contentAlignment = Alignment.Center
    ) {
        SplashContent(contentAlpha = contentAlpha.value)
    }
}

@Composable
private fun SplashContent(contentAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashGradient")
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "gradientRotation",
    )

    val logoPProgress = remember { Animatable(0f) }
    val logoXProgress = remember { Animatable(0f) }
    val ringProgress = remember { Animatable(0f) }
    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        logoPProgress.animateTo(1f, animationSpec = tween(500, delayMillis = 200))
        logoXProgress.animateTo(1f, animationSpec = tween(500, delayMillis = 100))
        ringProgress.animateTo(1f, animationSpec = tween(800, delayMillis = 300))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentTipIndex = (currentTipIndex + 1) % splashTips.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = contentAlpha)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(
                    PxColors.Primary.copy(alpha = 0.06f),
                    PxColors.Secondary.copy(alpha = 0.04f),
                    PxColors.PrimaryVariant.copy(alpha = 0.06f),
                    PxColors.Primary.copy(alpha = 0.06f),
                ),
                center = Offset(size.width / 2f, size.height / 2f),
            )
            drawCircle(
                brush = sweepBrush,
                radius = size.maxDimension * 0.7f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated logo
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    val strokeWidth = 5.dp.toPx()
                    val halfSize = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val logoSize = halfSize * 0.7f

                    // Draw P
                    val pPath = Path().apply {
                        val startX = center.x - logoSize * 0.5f
                        val topY = center.y - logoSize * 0.7f
                        val bottomY = center.y + logoSize * 0.7f
                        val pWidth = logoSize * 0.6f

                        // Vertical stem of P
                        moveTo(startX, topY)
                        lineTo(startX, bottomY)

                        // Top horizontal of P
                        moveTo(startX, topY)
                        lineTo(startX + pWidth, topY)

                        // Curve of P
                        moveTo(startX + pWidth, topY)
                        cubicTo(
                            startX + pWidth + logoSize * 0.3f, topY,
                            startX + pWidth + logoSize * 0.3f, center.y,
                            startX + pWidth * 0.5f, center.y,
                        )

                        // Bottom horizontal of P opening
                        moveTo(startX + pWidth * 0.5f, center.y)
                        lineTo(startX, center.y)
                    }

                    // Draw X
                    val xPath = Path().apply {
                        val xStartX = center.x + logoSize * 0.1f
                        val xEndX = center.x + logoSize * 0.9f
                        val xTopY = center.y - logoSize * 0.7f
                        val xBottomY = center.y + logoSize * 0.7f

                        moveTo(xStartX, xTopY)
                        lineTo(xEndX, xBottomY)

                        moveTo(xEndX, xTopY)
                        lineTo(xStartX, xBottomY)
                    }

                    drawPath(
                        path = pPath,
                        color = PxColors.Primary.copy(alpha = logoPProgress.value),
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )

                    drawPath(
                        path = xPath,
                        color = PxColors.Secondary.copy(alpha = logoXProgress.value),
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = PxColors.OnBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = PxColors.OnSurfaceDim,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Progress ring
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(48.dp)) {
                    val sweep = ringProgress.value * 360f
                    val ringStroke = 3.dp.toPx()
                    drawArc(
                        color = PxColors.Outline.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = ringStroke, cap = StrokeCap.Round),
                        topLeft = Offset(ringStroke / 2f, ringStroke / 2f),
                        size = Size(size.width - ringStroke, size.height - ringStroke),
                    )
                    drawArc(
                        color = PxColors.Primary,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = ringStroke, cap = StrokeCap.Round),
                        topLeft = Offset(ringStroke / 2f, ringStroke / 2f),
                        size = Size(size.width - ringStroke, size.height - ringStroke),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tips carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = currentTipIndex,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    label = "splashTip",
                ) { index ->
                    Text(
                        text = splashTips[index].text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnSurfaceDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun SplashScreenPreview() {
    ProductivityXTheme { SplashContent(contentAlpha = 1f) }
}
