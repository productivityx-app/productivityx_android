package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
enum class PxElevationLevel(val dp: Dp) {
    Level0(0.dp),
    Level1(2.dp),
    Level2(4.dp),
    Level3(8.dp),
    Level4(16.dp),
}

object ElevationTokens {
    val CardRest    = PxElevationLevel.Level1
    val CardPressed = PxElevationLevel.Level3
    val FAB         = PxElevationLevel.Level3
    val Dialog      = PxElevationLevel.Level4
    val BottomSheet = PxElevationLevel.Level2
    val Button      = PxElevationLevel.Level1
    val TopBar      = PxElevationLevel.Level0
    val Dropdown    = PxElevationLevel.Level3
}

data class ShadowDefinition(
    val ambientAlpha: Float,
    val spotAlpha: Float,
    val offsetY: Dp,
    val blurRadius: Dp,
)

object ShadowTokens {
    val CardRest = ShadowDefinition(
        ambientAlpha = 0.08f,
        spotAlpha = 0.12f,
        offsetY = 1.dp,
        blurRadius = 3.dp,
    )
    val CardPressed = ShadowDefinition(
        ambientAlpha = 0.12f,
        spotAlpha = 0.18f,
        offsetY = 4.dp,
        blurRadius = 12.dp,
    )
    val FAB = ShadowDefinition(
        ambientAlpha = 0.14f,
        spotAlpha = 0.20f,
        offsetY = 6.dp,
        blurRadius = 16.dp,
    )
    val Dialog = ShadowDefinition(
        ambientAlpha = 0.20f,
        spotAlpha = 0.24f,
        offsetY = 8.dp,
        blurRadius = 24.dp,
    )
    val BottomSheet = ShadowDefinition(
        ambientAlpha = 0.12f,
        spotAlpha = 0.16f,
        offsetY = 4.dp,
        blurRadius = 12.dp,
    )

    fun forLevel(level: PxElevationLevel, isDark: Boolean): ShadowDefinition = when (level) {
        PxElevationLevel.Level0 -> ShadowDefinition(0f, 0f, 0.dp, 0.dp)
        PxElevationLevel.Level1 -> CardRest.copy(
            ambientAlpha = if (isDark) 0.12f else 0.08f,
            spotAlpha = if (isDark) 0.10f else 0.12f,
        )
        PxElevationLevel.Level2 -> BottomSheet.copy(
            ambientAlpha = if (isDark) 0.16f else 0.12f,
            spotAlpha = if (isDark) 0.14f else 0.16f,
        )
        PxElevationLevel.Level3 -> CardPressed.copy(
            ambientAlpha = if (isDark) 0.18f else 0.12f,
            spotAlpha = if (isDark) 0.16f else 0.18f,
        )
        PxElevationLevel.Level4 -> Dialog.copy(
            ambientAlpha = if (isDark) 0.24f else 0.20f,
            spotAlpha = if (isDark) 0.20f else 0.24f,
        )
    }
}

fun Modifier.pxElevation(level: PxElevationLevel, shape: Shape? = null): Modifier {
    val elevation = level.dp
    return this.then(
        if (elevation > 0.dp) {
            Modifier.shadow(elevation, shape = shape ?: androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
        } else this
    )
}

fun Modifier.pxColoredShadow(
    color: Color,
    intensity: Float = 0.15f,
    blurRadius: Dp = 8.dp,
    offsetY: Dp = 2.dp,
): Modifier = this.then(
    Modifier.shadow(
        elevation = blurRadius,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        ambientColor = color.copy(alpha = intensity * 0.5f),
        spotColor = color.copy(alpha = intensity),
    )
)

@Stable
class PressElevationState(
    private val initial: PxElevationLevel,
    private val pressed: PxElevationLevel,
) {
    var currentLevel by mutableStateOf(initial)
        private set

    suspend fun onPress() {
        currentLevel = pressed
    }

    suspend fun onRelease() {
        currentLevel = initial
    }
}

@Composable
fun rememberPressElevationState(
    initialLevel: PxElevationLevel = ElevationTokens.CardRest,
    pressedLevel: PxElevationLevel = ElevationTokens.CardPressed,
): PressElevationState = remember(initialLevel, pressedLevel) {
    PressElevationState(initialLevel, pressedLevel)
}

fun Modifier.pxElevationClickFeedback(
    state: PressElevationState,
    shape: Shape? = null,
    isDark: Boolean = false,
): Modifier = this.then(
    Modifier
        .pxElevation(state.currentLevel, shape)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    state.onPress()
                    tryAwaitRelease()
                    state.onRelease()
                }
            )
        }
)

@Composable
fun PxElevationWrapper(
    level: PxElevationLevel,
    shape: Shape? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val elevationDp = level.dp
    val absoluteElevation = LocalAbsoluteTonalElevation.current + elevationDp

    CompositionLocalProvider(LocalAbsoluteTonalElevation provides absoluteElevation) {
        Box(
            modifier = Modifier.pxElevation(level, shape),
            content = content,
        )
    }
}
