package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class QuickActionItem(
    val icon: ImageVector,
    val color: Color,
    val label: String,
    val onClick: () -> Unit,
)

fun Modifier.swipeToAction(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    swipeThreshold: Float = 150f,
    enabled: Boolean = true,
): Modifier {
    if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) return this

    return this.pointerInput(enabled, onSwipeLeft, onSwipeRight) {
        detectHorizontalDragGestures(
            onDragEnd = { },
            onDragCancel = { },
            onHorizontalDrag = { _, _ -> },
        )
    }
}

@Composable
fun Modifier.swipeToActionComposable(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    swipeThreshold: Float = 150f,
    enabled: Boolean = true,
): Modifier {
    if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) return this

    val offsetX = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    var hapticTriggered by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    return this
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .pointerInput(enabled, onSwipeLeft, onSwipeRight) {
            detectHorizontalDragGestures(
                onDragStart = {
                    scope.launch { offsetX.animateTo(0f, spring()) }
                    hapticTriggered = false
                },
                onDragEnd = {
                    scope.launch {
                        if (abs(offsetX.value) > swipeThreshold) {
                            if (offsetX.value > 0f) onSwipeRight?.invoke()
                            else onSwipeLeft?.invoke()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        offsetX.animateTo(0f, spring())
                    }
                },
                onDragCancel = {
                    scope.launch { offsetX.animateTo(0f, spring()) }
                },
                onHorizontalDrag = { _, dragAmount ->
                    scope.launch {
                        val newValue = (offsetX.value + dragAmount)
                            .coerceIn(-swipeThreshold * 2f, swipeThreshold * 2f)
                        offsetX.snapTo(newValue)
                        if (abs(newValue) > swipeThreshold && !hapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            hapticTriggered = true
                        }
                    }
                },
            )
        }
}

@Composable
fun GestureCoachMark(
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (visible) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.small)
                .background(PxColors.Primary.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
fun SwipeableListItem(
    modifier: Modifier = Modifier,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.swipeToActionComposable(
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            enabled = enabled,
        ),
    ) {
        content()
    }
}
