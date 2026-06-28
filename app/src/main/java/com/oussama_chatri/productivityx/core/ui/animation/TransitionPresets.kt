package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

object TransitionDefaults {
    val springSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    val slideSpringSpec: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    val fastTween = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing)
    val slowTween = tween<Float>(durationMillis = 400, easing = FastOutSlowInEasing)
    val slideTween = tween<IntOffset>(durationMillis = 250, easing = FastOutSlowInEasing)
    val slideSlowTween = tween<IntOffset>(durationMillis = 400, easing = FastOutSlowInEasing)
}

object TransitionPresets {

    val fadeThrough = object {
        val enter: EnterTransition = fadeIn(TransitionDefaults.fastTween)
        val exit: ExitTransition = fadeOut(TransitionDefaults.fastTween)
        val popEnter: EnterTransition = fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition = fadeOut(TransitionDefaults.fastTween)
    }

    val slideHorizontal = object {
        val enter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideTween) + fadeIn(TransitionDefaults.fastTween)
        val exit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideTween) { -it / 3 } + fadeOut(TransitionDefaults.fastTween)
        val popEnter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideTween) { -it / 3 } + fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideTween) + fadeOut(TransitionDefaults.fastTween)
    }

    val slideVertical = object {
        val enter: EnterTransition =
            slideInVertically(TransitionDefaults.slideTween) + fadeIn(TransitionDefaults.fastTween)
        val exit: ExitTransition =
            slideOutVertically(TransitionDefaults.slideTween) { -it / 3 } + fadeOut(TransitionDefaults.fastTween)
        val popEnter: EnterTransition =
            slideInVertically(TransitionDefaults.slideTween) { -it / 3 } + fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition =
            slideOutVertically(TransitionDefaults.slideTween) + fadeOut(TransitionDefaults.fastTween)
    }

    val scale = object {
        val enter: EnterTransition =
            scaleIn(TransitionDefaults.springSpec, initialScale = 0.92f) + fadeIn(TransitionDefaults.fastTween)
        val exit: ExitTransition =
            scaleOut(TransitionDefaults.springSpec, targetScale = 0.92f) + fadeOut(TransitionDefaults.fastTween)
        val popEnter: EnterTransition =
            scaleIn(TransitionDefaults.springSpec, initialScale = 0.92f) + fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition =
            scaleOut(TransitionDefaults.springSpec, targetScale = 0.92f) + fadeOut(TransitionDefaults.fastTween)
    }

    val slideUpSheet = object {
        val enter: EnterTransition =
            slideInVertically(TransitionDefaults.slideSlowTween) + fadeIn(TransitionDefaults.slowTween)
        val exit: ExitTransition =
            slideOutVertically(TransitionDefaults.slideSlowTween) + fadeOut(TransitionDefaults.slowTween)
        val popEnter: EnterTransition = fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition = fadeOut(TransitionDefaults.fastTween)
    }

    val detailPush = object {
        val enter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideSpringSpec) { it / 4 } +
                    fadeIn(TransitionDefaults.springSpec) +
                    scaleIn(TransitionDefaults.springSpec, initialScale = 0.96f)
        val exit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideTween) { -it / 3 } +
                    fadeOut(TransitionDefaults.fastTween)
        val popEnter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideTween) { -it / 3 } +
                    fadeIn(TransitionDefaults.fastTween)
        val popExit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideSpringSpec) { it / 4 } +
                    fadeOut(TransitionDefaults.springSpec) +
                    scaleOut(TransitionDefaults.springSpec, targetScale = 0.96f)
    }

    val authForm = object {
        val enter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideTween) + fadeIn(TransitionDefaults.fastTween)
        val popEnter: EnterTransition =
            slideInHorizontally(TransitionDefaults.slideTween) { -it } + fadeIn(TransitionDefaults.fastTween)
        val exit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideTween) { -it / 3 } + fadeOut(TransitionDefaults.fastTween)
        val popExit: ExitTransition =
            slideOutHorizontally(TransitionDefaults.slideTween) { it / 3 } + fadeOut(TransitionDefaults.fastTween)
    }

    val tabSwitch = object {
        val enter: EnterTransition =
            fadeIn(TransitionDefaults.fastTween) + scaleIn(TransitionDefaults.springSpec, initialScale = 0.97f)
        val exit: ExitTransition =
            fadeOut(TransitionDefaults.fastTween) + scaleOut(TransitionDefaults.springSpec, targetScale = 0.97f)
    }
}
