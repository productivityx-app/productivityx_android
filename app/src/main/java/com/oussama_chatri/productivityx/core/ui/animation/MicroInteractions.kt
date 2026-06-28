package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class MicroAnimationConfig(
    val scaleOnPress: Boolean = true,
    val scaleTarget: Float = 0.96f,
    val elevationOnPress: Boolean = true,
    val hapticOnPress: Boolean = true,
    val rippleEnabled: Boolean = true,
)

object MicroAnimationDefaults {
    fun pressSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    fun toggleSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
}

@Composable
fun PxAnimatedToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(),
        label = "toggleThumb",
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(),
        label = "toggleTrack",
    )
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = {
                    if (enabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCheckedChange(!checked)
                    }
                },
                role = Role.Switch,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
        }
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trackColor),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = (thumbOffset * 20).dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .shadow(2.dp, CircleShape),
            )
        }
    }
}

@Composable
fun PxAnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    iconSize: Dp = 22.dp,
) {
    val checkAnim = remember { Animatable(if (checked) 1f else 0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(checked) {
        haptic.performHapticFeedback(
            if (checked) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
        )
        checkAnim.animateTo(
            targetValue = if (checked) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        )
    }

    val bgColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = spring(),
        label = "checkboxBg",
    )

    Canvas(
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox,
            ),
    ) {
        drawRoundRect(
            color = bgColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
        )

        if (checkAnim.value > 0f) {
            val progress = checkAnim.value
            val canvasSize = this.size
            val path = Path().apply {
                moveTo(canvasSize.width * 0.2f, canvasSize.height * 0.5f)
                lineTo(canvasSize.width * 0.4f, canvasSize.height * 0.7f)
                lineTo(canvasSize.width * 0.8f, canvasSize.height * 0.3f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                ),
                alpha = progress,
            )
        }
    }
}

@Composable
fun PxAnimatedRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    size: Dp = 22.dp,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "radioScale",
    )
    val haptic = LocalHapticFeedback.current

    Canvas(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                role = Role.RadioButton,
            ),
    ) {
        val outerRadius = size.toPx() / 2f
        val innerRadius = outerRadius * 0.55f

        drawCircle(
            color = if (selected) selectedColor else unselectedColor,
            radius = outerRadius,
            style = Stroke(width = 2.dp.toPx()),
        )

        if (scale > 0f) {
            drawCircle(
                color = selectedColor,
                radius = innerRadius * scale,
            )
        }
    }
}

@Composable
fun Modifier.pxPressAnimation(
    interactionSource: MutableInteractionSource,
    scaleTarget: Float = 0.96f,
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleTarget else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pressScale",
    )
    return this.then(Modifier.scale(scale))
}
