package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.Dimensions
import com.oussama_chatri.productivityx.core.ui.theme.ElevationTokens
import com.oussama_chatri.productivityx.core.ui.theme.LayoutTokens
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.PxElevationLevel
import com.oussama_chatri.productivityx.core.ui.theme.Spacing
import com.oussama_chatri.productivityx.core.ui.theme.pxElevation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================== CARD SYSTEM (Prompt 2.3) ====================

enum class PxCardVariant { Standard, Outlined, Elevated, Filled }

enum class PxCardLayout { Vertical, Horizontal, Compact }

@Composable
fun PxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: PxCardVariant = PxCardVariant.Standard,
    layout: PxCardLayout = PxCardLayout.Vertical,
    isSelected: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    elevationLevel: PxElevationLevel = ElevationTokens.CardRest,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> PxColors.Primary.copy(alpha = 0.1f)
            variant == PxCardVariant.Filled -> PxColors.SurfaceVariant
            else -> PxColors.Surface
        },
        animationSpec = tween(200),
        label = "cardBg",
    )
    val elevation = when (variant) {
        PxCardVariant.Elevated -> elevationLevel.dp
        else -> 0.dp
    }

    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (variant == PxCardVariant.Outlined) {
            androidx.compose.foundation.BorderStroke(1.dp, PxColors.Outline)
        } else null,
        content = content,
    )
}

@Composable
fun PxDashboardCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trend: Float? = null,
) {
    PxCard(modifier = modifier, variant = PxCardVariant.Standard) {
        Column(modifier = Modifier.padding(LayoutTokens.CardPadding)) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = PxColors.Primary,
                )
                Spacer(Modifier.height(Spacing.sm))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = PxColors.OnBackground,
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim,
            )
            if (trend != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = if (trend >= 0) "+${(trend * 100).toInt()}%" else "${(trend * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (trend >= 0) PxColors.Success else PxColors.Error,
                )
            }
        }
    }
}

@Composable
fun PxCardGroup(
    modifier: Modifier = Modifier,
    count: Int = 3,
    offset: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        for (i in (1..count).reversed()) {
            Card(
                modifier = Modifier
                    .offset(y = offset * i)
                    .alpha(1f - i * 0.15f),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = PxColors.SurfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {}
        }
        Card(
            modifier = Modifier.clickable {},
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.CardRest.dp),
            content = content,
        )
    }
}

// ==================== LOADING & EMPTY STATE (Prompt 2.4) ====================

@Composable
fun PxSkeleton(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(shape)
            .background(PxColors.SurfaceVariant.copy(alpha = alpha)),
    )
}

@Composable
fun PxSkeletonCard(modifier: Modifier = Modifier) {
    PxCard(modifier = modifier) {
        Column(modifier = Modifier.padding(LayoutTokens.CardPadding)) {
            PxSkeleton(height = 20.dp, width = 120.dp)
            Spacer(Modifier.height(Spacing.sm))
            PxSkeleton(height = 14.dp)
            Spacer(Modifier.height(Spacing.xxs))
            PxSkeleton(height = 14.dp, width = 180.dp)
            Spacer(Modifier.height(Spacing.md))
            PxSkeleton(height = 80.dp, shape = RoundedCornerShape(8.dp))
        }
    }
}

enum class ContentLoadState { Loading, Success, Error, Empty }

@Composable
fun ContentState(
    state: ContentLoadState,
    modifier: Modifier = Modifier,
    loadingContent: @Composable (() -> Unit)? = null,
    errorMessage: String = "Something went wrong",
    emptyTitle: String = "Nothing here yet",
    emptySubtitle: String? = null,
    emptyIcon: ImageVector = Icons.Outlined.Inbox,
    onRetry: (() -> Unit)? = null,
    emptyAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "contentState",
    ) { currentState ->
        when (currentState) {
            ContentLoadState.Loading -> {
                loadingContent?.invoke() ?: PxLoadingState()
            }
            ContentLoadState.Error -> {
                PxErrorState(message = errorMessage, onRetry = onRetry)
            }
            ContentLoadState.Empty -> {
                PxEmptyState(
                    icon = emptyIcon,
                    title = emptyTitle,
                    subtitle = emptySubtitle,
                    actionLabel = null,
                    onAction = null,
                )
                emptyAction?.invoke()
            }
            ContentLoadState.Success -> content()
        }
    }
}

@Composable
fun PxErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = PxColors.Error.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurface,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(Spacing.xxl))
            PxButton(
                text = stringResource(R.string.retry),
                onClick = onRetry,
                variant = PxButtonVariant.Outlined,
                leadingIcon = Icons.Filled.Refresh,
            )
        }
    }
}

// ==================== DIALOG & BOTTOM SHEET (Prompt 2.5) ====================

@Composable
fun PxDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.confirm),
    dismissLabel: String = stringResource(R.string.cancel),
    onConfirm: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    showCloseButton: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = PxColors.Surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (isDestructive) PxColors.Error else PxColors.Primary,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = PxColors.OnBackground,
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface,
            )
        },
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = { onConfirm(); onDismiss() }) {
                    Text(
                        confirmLabel,
                        color = if (isDestructive) PxColors.Error else PxColors.Primary,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, color = PxColors.OnSurfaceDim)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PxBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            containerColor = PxColors.Surface,
            shape = MaterialTheme.shapes.large,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PxColors.Outline.copy(alpha = 0.5f)),
                )
            },
            content = content,
        )
    }
}

@Composable
fun PxBottomSheetMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    items: List<PxDropdownItem>,
    modifier: Modifier = Modifier,
) {
    PxBottomSheet(visible = visible, onDismiss = onDismiss, modifier = modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnBackground,
                modifier = Modifier.padding(LayoutTokens.ScreenPadding),
            )
            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.3f))
        }
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isDestructive) PxColors.Error else PxColors.OnSurface,
                    )
                },
                leadingIcon = item.icon?.let { icon ->
                    @Composable { Icon(icon, null, Modifier.size(Dimensions.iconMd), tint = if (item.isDestructive) PxColors.Error else PxColors.OnSurfaceDim) }
                },
                onClick = { item.onClick(); onDismiss() },
            )
        }
    }
}

data class PxDropdownItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
)

// ==================== LEGACY COMPONENTS ====================

@Composable
fun PxEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = PxColors.OnSurfaceDim.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xxl))
            PxButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun PxLoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = PxColors.Primary)
    }
}

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
                shape = MaterialTheme.shapes.medium,
            )
        },
    )
}

@Composable
fun PxDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<PxDropdownItem>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(PxColors.Surface),
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isDestructive) PxColors.Error else PxColors.OnSurface,
                    )
                },
                leadingIcon = item.icon?.let { icon ->
                    @Composable { Icon(icon, null, Modifier.size(Dimensions.iconMd), tint = if (item.isDestructive) PxColors.Error else PxColors.OnSurfaceDim) }
                },
                onClick = { item.onClick(); onDismiss() },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxComponentsPreview() {
    ProductivityXTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PxCard {
                Text("Standard Card", style = MaterialTheme.typography.titleMedium, color = PxColors.OnBackground)
            }
            PxCard(variant = PxCardVariant.Elevated) {
                Text("Elevated Card", style = MaterialTheme.typography.titleMedium, color = PxColors.OnBackground)
            }
            PxSkeletonCard()
            PxDashboardCard(title = "Tasks Completed", value = "24", icon = Icons.Filled.Check, trend = 0.12f)
        }
    }
}
