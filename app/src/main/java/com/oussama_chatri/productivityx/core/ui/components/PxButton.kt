package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.PxIcons
import com.oussama_chatri.productivityx.core.ui.theme.PxIconSizes

enum class PxButtonVariant { Primary, Outlined, Ghost, Tonal, Elevated, Destructive }

enum class PxButtonSize {
    Small,
    Medium,
    Large,
}

@Composable
fun PxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    variant: PxButtonVariant = PxButtonVariant.Primary,
    size: PxButtonSize = PxButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isToggleSelected: Boolean? = null,
    onToggleChange: ((Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonScale",
    )

    val isEnabled = enabled && !isLoading
    val shape = RoundedCornerShape(8.dp)
    val heightDp: Dp = when (size) {
        PxButtonSize.Small -> 36.dp
        PxButtonSize.Medium -> 48.dp
        PxButtonSize.Large -> 56.dp
    }
    val contentPadding = when (size) {
        PxButtonSize.Small -> PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        PxButtonSize.Medium -> PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        PxButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 0.dp)
    }
    val iconSize = when (size) {
        PxButtonSize.Small -> PxIconSizes.sm
        PxButtonSize.Medium -> PxIconSizes.md
        PxButtonSize.Large -> PxIconSizes.lg
    }

    val currentEnabled = if (isToggleSelected != null) true else isEnabled
    val currentOnClick = if (isToggleSelected != null && onToggleChange != null) {
        { onToggleChange(!isToggleSelected) }
    } else onClick

    val disableAlpha = 0.5f
    val boxModifier = Modifier
        .scale(scale)
        .alpha(if (currentEnabled) 1f else disableAlpha)

    when (variant) {
        PxButtonVariant.Primary -> Button(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = PxColors.Primary,
                contentColor = Color.White,
                disabledContainerColor = PxColors.Primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, Color.White) }

        PxButtonVariant.Outlined -> OutlinedButton(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PxColors.Primary,
                disabledContentColor = PxColors.OnSurfaceDim,
            ),
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, PxColors.Primary) }

        PxButtonVariant.Ghost -> TextButton(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, PxColors.Primary) }

        PxButtonVariant.Tonal -> Button(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = PxColors.SurfaceVariant,
                contentColor = PxColors.Primary,
            ),
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, PxColors.Primary) }

        PxButtonVariant.Elevated -> Button(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = PxColors.Surface,
                contentColor = PxColors.Primary,
                disabledContainerColor = PxColors.Surface.copy(alpha = 0.5f),
            ),
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, PxColors.Primary) }

        PxButtonVariant.Destructive -> Button(
            onClick = currentOnClick,
            modifier = modifier.height(heightDp),
            enabled = currentEnabled,
            shape = shape,
            interactionSource = interactionSource,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = PxColors.Error,
                contentColor = Color.White,
                disabledContainerColor = PxColors.Error.copy(alpha = 0.5f),
            ),
        ) { ButtonContent(text, isLoading, size, iconSize, leadingIcon, trailingIcon, Color.White) }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    size: PxButtonSize,
    iconSize: Dp,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    tint: Color,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(iconSize),
            color = tint,
            strokeWidth = 2.dp,
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = tint,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontWeight = if (size == PxButtonSize.Small) FontWeight.Medium else FontWeight.SemiBold,
                style = when (size) {
                    PxButtonSize.Small -> MaterialTheme.typography.labelLarge
                    PxButtonSize.Medium -> MaterialTheme.typography.bodyLarge
                    PxButtonSize.Large -> MaterialTheme.typography.titleMedium
                },
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = tint,
                )
            }
        }
    }
}

@Composable
fun PxIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = PxColors.OnSurface,
    size: PxButtonSize = PxButtonSize.Medium,
    enabled: Boolean = true,
    variant: PxButtonVariant = PxButtonVariant.Ghost,
) {
    val iconSize = when (size) {
        PxButtonSize.Small -> PxIconSizes.sm
        PxButtonSize.Medium -> PxIconSizes.md
        PxButtonSize.Large -> PxIconSizes.lg
    }
    val btnSize = when (size) {
        PxButtonSize.Small -> 36.dp
        PxButtonSize.Medium -> 48.dp
        PxButtonSize.Large -> 56.dp
    }
    val containerColor = when (variant) {
        PxButtonVariant.Primary, PxButtonVariant.Destructive -> PxColors.Primary
        PxButtonVariant.Tonal -> PxColors.SurfaceVariant
        else -> Color.Transparent
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(btnSize),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = tint,
            disabledContentColor = tint.copy(alpha = 0.5f),
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun PxButtonGroup(
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (vertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}

@Composable
fun PxToggleButton(
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String? = null,
    icon: ImageVector? = null,
    variant: PxButtonVariant = PxButtonVariant.Outlined,
) {
    val actualVariant = if (isSelected) PxButtonVariant.Primary else variant
    PxButton(
        text = text ?: "",
        onClick = { onToggle(!isSelected) },
        modifier = modifier,
        enabled = enabled,
        variant = actualVariant,
        leadingIcon = icon,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxButtonPreview() {
    ProductivityXTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PxButton(text = "Primary", onClick = {})
            PxButton(text = "Loading", onClick = {}, isLoading = true)
            PxButton(text = "With Icon", onClick = {}, leadingIcon = PxIcons.Action.Add)
            PxButton(text = "Tonal", onClick = {}, variant = PxButtonVariant.Tonal)
            PxButton(text = "Destructive", onClick = {}, variant = PxButtonVariant.Destructive)
            PxButton(text = "Small", onClick = {}, size = PxButtonSize.Small)
            PxButton(text = "Large", onClick = {}, size = PxButtonSize.Large)
            PxButtonGroup {
                PxButton(text = "Save", onClick = {}, variant = PxButtonVariant.Primary)
                PxButton(text = "Cancel", onClick = {}, variant = PxButtonVariant.Outlined)
            }
        }
    }
}
