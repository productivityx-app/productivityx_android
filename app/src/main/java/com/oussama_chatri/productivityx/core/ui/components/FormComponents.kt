package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.core.ui.theme.Dimensions
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.initials

// OtpInputField

@Composable
fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    onComplete: ((String) -> Unit)? = null
) {
    val focusRequesters = remember { List(length) { FocusRequester() } }

    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (new.length <= length && new.all { it.isDigit() }) {
                onValueChange(new)
                if (new.length == length) onComplete?.invoke(new)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        decorationBox = {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    val isFocused = value.length == index

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 48.dp, height = 56.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(PxColors.SurfaceVariant)
                            .border(
                                width = 2.dp,
                                color = if (isFocused) PxColors.Primary else PxColors.Outline,
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            color = PxColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

// PasswordStrengthIndicator

@Composable
fun PasswordStrengthIndicator(password: String, modifier: Modifier = Modifier) {
    val conditions = listOf(
        password.length >= 8,
        password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() }
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        conditions.forEach { met ->
            val color by animateColorAsState(
                targetValue = if (met) PxColors.Success else PxColors.Outline,
                animationSpec = tween(200),
                label = "strengthDot"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// StepIndicator

@Composable
fun StepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isActive = index == currentStep

            val circleColor by animateColorAsState(
                targetValue = when {
                    isCompleted || isActive -> PxColors.Primary
                    else -> PxColors.Outline
                },
                animationSpec = tween(200),
                label = "stepColor"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) Color.White else PxColors.OnSurfaceDim
                    )
                }
            }

            if (index < totalSteps - 1) {
                val lineColor by animateColorAsState(
                    targetValue = if (isCompleted) PxColors.Primary else PxColors.Outline,
                    animationSpec = tween(200),
                    label = "stepLine"
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

// AvatarImage

@Composable
fun AvatarImage(
    imageUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = Dimensions.avatarMd
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(PxColors.Primary, PxColors.Secondary)
                )
            )
            .border(2.dp, PxColors.Primary, CircleShape)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Text(
                text = displayName.initials(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

// MinuteStepper

@Composable
fun MinuteStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 120,
    step: Int = 5
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { if (value - step >= min) onValueChange(value - step) },
            enabled = value - step >= min
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = "Decrease",
                tint = if (value - step >= min) PxColors.OnSurface else PxColors.Outline
            )
        }

        Text(
            text = "$value min",
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnBackground,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { if (value + step <= max) onValueChange(value + step) },
            enabled = value + step <= max
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Increase",
                tint = if (value + step <= max) PxColors.OnSurface else PxColors.Outline
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun FormComponentsPreview() {
    ProductivityXTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OtpInputField(value = "123", onValueChange = {})
            PasswordStrengthIndicator(password = "Hello1!")
            StepIndicator(totalSteps = 4, currentStep = 2, modifier = Modifier.fillMaxWidth())
            AvatarImage(imageUrl = null, displayName = "Oussama Chatri")
            MinuteStepper(value = 25, onValueChange = {})
        }
    }
}