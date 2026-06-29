package com.oussama_chatri.productivityx.features.auth.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxButtonVariant
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int,
    val accentColor: Color,
)

private val pages = listOf(
    OnboardingPageData(
        icon = Icons.Outlined.StickyNote2,
        titleRes = R.string.onboarding_page1_title,
        bodyRes = R.string.onboarding_page1_body,
        accentColor = Color(0xFF6366F1),
    ),
    OnboardingPageData(
        icon = Icons.Outlined.Timer,
        titleRes = R.string.onboarding_page2_title,
        bodyRes = R.string.onboarding_page2_body,
        accentColor = Color(0xFFF59E0B),
    ),
    OnboardingPageData(
        icon = Icons.Outlined.AutoAwesome,
        titleRes = R.string.onboarding_page3_title,
        bodyRes = R.string.onboarding_page3_body,
        accentColor = Color(0xFF22C55E),
    ),
)

private val themeOptions = listOf(
    "DARK" to "Dark",
    "LIGHT" to "Light",
    "OCEAN" to "Ocean",
    "AMBER" to "Amber",
    "FOREST" to "Forest",
    "ROSE" to "Rose",
    "MIDNIGHT" to "Midnight",
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val totalPageCount = pages.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPageCount })
    val scope = rememberCoroutineScope()
    var selectedTheme by remember { mutableStateOf("DARK") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background)
    ) {
        // Parallax background
        ParallaxBackground(pagerState = pagerState)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->
                if (index < pages.size) {
                    OnboardingPageContent(
                        page = pages[index],
                        pageIndex = index,
                        pagerState = pagerState,
                    )
                } else {
                    GetStartedPage(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it },
                    )
                }
            }

            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (pagerState.currentPage < totalPageCount - 1) {
                    TextButton(onClick = {
                        viewModel.completeOnboarding()
                        onGetStarted()
                    }) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = PxColors.OnSurfaceDim,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(72.dp))
                }

                // Animated dot indicators
                if (pagerState.currentPage < pages.size) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(totalPageCount) { index ->
                            val isActive = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isActive) 24.dp else 8.dp,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                label = "dotWidth",
                            )
                            val color by animateFloatAsState(
                                targetValue = if (isActive) 1f else 0.2f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                label = "dotColor",
                            )
                            Box(
                                modifier = Modifier
                                    .width(width)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(PxColors.Primary.copy(alpha = color)),
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                if (pagerState.currentPage < totalPageCount - 1) {
                    PxButton(
                        text = stringResource(R.string.onboarding_next),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    )
                } else {
                    PxButton(
                        text = stringResource(R.string.onboarding_get_started),
                        onClick = {
                            viewModel.completeOnboarding()
                            onGetStarted()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ParallaxBackground(pagerState: PagerState) {
    val currentPage = pagerState.currentPage
    val pageOffset = pagerState.currentPageOffsetFraction
    val offset = (currentPage + pageOffset)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val parallaxFactor = 0.3f

        // Floating circles that move with parallax
        val circlePositions = listOf(
            Offset(size.width * 0.8f, size.height * 0.15f),
            Offset(size.width * 0.15f, size.height * 0.25f),
            Offset(size.width * 0.85f, size.height * 0.75f),
            Offset(size.width * 0.2f, size.height * 0.8f),
        )

        circlePositions.forEachIndexed { i, pos ->
            val factor = (i + 1) * 0.15f
            val dx = offset * parallaxFactor * factor * size.width * 0.02f
            val dy = offset * parallaxFactor * factor * size.height * 0.01f
            drawCircle(
                color = PxColors.Primary.copy(alpha = 0.04f),
                radius = (40 + i * 20).dp.toPx(),
                center = Offset(pos.x + dx, pos.y + dy),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPageData,
    pageIndex: Int,
    pagerState: PagerState,
) {
    val isActive = pagerState.currentPage == pageIndex

    val iconScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            iconScale.snapTo(0f)
            contentAlpha.snapTo(0f)
            iconScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f))
            contentAlpha.animateTo(1f, animationSpec = tween(400))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .graphicsLayer { alpha = contentAlpha.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Animated icon with gradient background
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(iconScale.value)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.2f),
                            page.accentColor.copy(alpha = 0.05f),
                        ),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = page.accentColor,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = PxColors.OnBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Interactive demo per page
        when (pageIndex) {
            0 -> NotesDemo(isActive = isActive)
            1 -> PomodoroDemo(isActive = isActive)
            2 -> AiDemo(isActive = isActive)
        }
    }
}

@Composable
private fun NotesDemo(isActive: Boolean) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(isActive) {
        if (isActive) {
            delay(300)
            visible.value = true
        } else {
            visible.value = false
        }
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(tween(400)) + scaleIn(tween(300)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PxColors.Surface)
                .padding(16.dp),
        ) {
            Text(
                text = "Quick Note",
                style = MaterialTheme.typography.titleSmall,
                color = PxColors.OnBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Buy groceries\n• Finish report\n• Call dentist",
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PxColors.Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "3 tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroDemo(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "timerProgress",
    )

    LaunchedEffect(isActive) { /* just for activation */ }

    Box(
        modifier = Modifier
            .size(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                color = PxColors.Outline.copy(alpha = 0.15f),
                radius = size.minDimension / 2f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
            drawArc(
                color = Color(0xFFF59E0B),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(2.5.dp.toPx(), 2.5.dp.toPx()),
                size = Size(
                    size.width - 5.dp.toPx(),
                    size.height - 5.dp.toPx(),
                ),
            )
        }
        Text(
            text = "${(progress * 25).toInt()}:${String.format("%02d", (59 - (progress * 59).toInt()))}",
            style = MaterialTheme.typography.labelMedium,
            color = PxColors.OnBackground,
        )
    }
}

@Composable
private fun AiDemo(isActive: Boolean) {
    var visibleTexts by remember { mutableStateOf(0) }

    LaunchedEffect(isActive) {
        if (isActive) {
            visibleTexts = 0
            delay(400)
            visibleTexts = 1
            delay(600)
            visibleTexts = 2
            delay(600)
            visibleTexts = 3
        } else {
            visibleTexts = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF22C55E),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AI Suggestions",
                style = MaterialTheme.typography.titleSmall,
                color = PxColors.OnBackground,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val suggestions = listOf(
            "Schedule meeting prep for tomorrow",
            "Break down report into subtasks",
            "Set focus block: 2pm - 4pm",
        )

        suggestions.forEachIndexed { index, suggestion ->
            AnimatedVisibility(
                visible = visibleTexts > index,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                exit = fadeOut(tween(200)),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun GetStartedPage(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            color = PxColors.OnBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your theme to personalize your experience",
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Theme selector grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            themeOptions.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { (key, label) ->
                        val isSelected = selectedTheme == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) PxColors.Primary.copy(alpha = 0.15f)
                                    else PxColors.Surface
                                )
                                .then(
                                    if (isSelected) Modifier.background(
                                        PxColors.Primary.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    ) else Modifier
                                )
                                .clickable { onThemeSelected(key) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "You can always change this later in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurfaceDim.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun OnboardingScreenPreview() {
    ProductivityXTheme { OnboardingScreen(onGetStarted = {}) }
}
