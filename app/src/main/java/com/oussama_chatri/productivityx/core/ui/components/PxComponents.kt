package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.Dimensions
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.Spacing

// PxCard

@Composable
fun PxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

// PxChip

@Composable
fun PxChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    selectedColor: Color = PxColors.Primary
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(150),
        label = "chipColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else PxColors.Outline,
        animationSpec = tween(150),
        label = "chipBorder"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else PxColors.OnSurfaceDim,
        animationSpec = tween(150),
        label = "chipContent"
    )

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
            labelColor = contentColor,
            containerColor = containerColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = borderColor,
            selectedBorderColor = borderColor
        )
    )
}

// PxEmptyState

@Composable
fun PxEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = PxColors.OnSurfaceDim.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurface,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xxl))
            PxButton(text = actionLabel, onClick = onAction)
        }
    }
}

// PxLoadingOverlay

@Composable
fun PxLoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PxColors.Primary)
    }
}


// PxLoadingState

@Composable
fun PxLoadingState(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color    = PxColors.Primary.copy(alpha = alpha),
            modifier = Modifier.size(40.dp),
        )
    }
}

// PxSnackbarHost

@Composable
fun PxSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = PxColors.SurfaceVariant,
                contentColor = PxColors.OnBackground,
                actionColor = PxColors.Primary,
                shape = MaterialTheme.shapes.medium
            )
        }
    )
}

// PxDialog

@Composable
fun PxDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.confirm),
    dismissLabel: String = stringResource(R.string.cancel),
    onConfirm: (() -> Unit)? = null,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = PxColors.Surface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PxColors.OnBackground
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface
            )
        },
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = { onConfirm(); onDismiss() }) {
                    Text(
                        confirmLabel,
                        color = if (isDestructive) PxColors.Error else PxColors.Primary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, color = PxColors.OnSurfaceDim)
            }
        }
    )
}

// PxDropdownMenu

data class PxDropdownItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun PxDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<PxDropdownItem>,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(PxColors.Surface)
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isDestructive) PxColors.Error else PxColors.OnSurface
                    )
                },
                leadingIcon = item.icon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (item.isDestructive) PxColors.Error else PxColors.OnSurfaceDim,
                            modifier = Modifier.size(Dimensions.iconMd)
                        )
                    }
                },
                onClick = { item.onClick(); onDismiss() }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxEmptyStatePreview() {
    ProductivityXTheme {
        PxEmptyState(
            icon = Icons.Outlined.Inbox,
            title = "Nothing here yet",
            subtitle = "Your content will appear here",
            actionLabel = "Get Started",
            onAction = {}
        )
    }
}