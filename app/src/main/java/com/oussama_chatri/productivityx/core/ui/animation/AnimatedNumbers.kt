package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.MaterialTheme

@Composable
fun AnimatedCounter(
    targetValue: Float,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    val displayText = "${prefix}${animatedValue.value.toInt()}$suffix"

    androidx.compose.material3.Text(
        text = displayText,
        style = MaterialTheme.typography.displaySmall,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}

@Composable
fun AnimatedCounterFloat(
    targetValue: Float,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    decimalPlaces: Int = 1,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    val format = "%." + decimalPlaces + "f"
    val displayText = "$prefix${String.format(format, animatedValue.value)}$suffix"

    androidx.compose.material3.Text(
        text = displayText,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}
