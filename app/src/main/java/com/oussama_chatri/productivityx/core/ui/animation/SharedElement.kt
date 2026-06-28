package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntOffset

private val sharedSpringSpec: FiniteAnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

data class SharedElementState(
    val isShared: Boolean = false,
    val contentKey: String = "",
)

@Composable
fun SharedTransitionHost(
    state: SharedElementState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedContent<String>(
        targetState = state.contentKey,
        transitionSpec = {
            if (targetState.isNotEmpty() && initialState.isNotEmpty()) {
                ContentTransform(
                    targetContentEnter = fadeIn(sharedSpringSpec),
                    initialContentExit = fadeOut(sharedSpringSpec),
                    sizeTransform = SizeTransform(clip = false),
                )
            } else {
                fadeIn(sharedSpringSpec) togetherWith fadeOut(sharedSpringSpec)
            }
        },
        label = "sharedTransitionHost",
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
@NonRestartableComposable
fun SharedElement(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

fun fadeThroughTransition(
    enterSlideDistance: () -> Int = { 30 },
): ContentTransform =
    (slideInVertically(spring<IntOffset>()) { it / 4 } + fadeIn(sharedSpringSpec)) togetherWith
            (slideOutVertically(spring<IntOffset>()) { -it / 4 } + fadeOut(sharedSpringSpec))

fun detailSlideUpTransition(): ContentTransform =
    (slideInVertically(spring<IntOffset>()) { it } + fadeIn(sharedSpringSpec)) togetherWith
            (slideOutVertically(spring<IntOffset>()) { it } + fadeOut(sharedSpringSpec))
