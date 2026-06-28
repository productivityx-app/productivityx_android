package com.oussama_chatri.productivityx.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

object StaggerDefaults {
    val staggerDelayMs = 50
    val entranceDurationMs = 350
    val slideOffset = 20.dp
}

@Composable
fun StaggeredListItem(
    index: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(!enabled) }
    LaunchedEffect(Unit) {
        if (enabled) {
            delay((index * StaggerDefaults.staggerDelayMs).toLong())
            visible = true
        }
    }

    val density = LocalDensity.current
    val slideOffsetPx = with(density) { StaggerDefaults.slideOffset.toPx().roundToInt() }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = StaggerDefaults.entranceDurationMs,
                easing = androidx.compose.animation.core.FastOutSlowInEasing,
            ),
            initialOffsetY = { it + slideOffsetPx }
        ) + fadeIn(
            animationSpec = tween(StaggerDefaults.entranceDurationMs)
        ),
        exit = slideOutVertically(
            animationSpec = tween(250),
            targetOffsetY = { -it / 3 }
        ) + fadeOut(tween(250)) + shrinkVertically(
            animationSpec = tween(250)
        ),
        modifier = modifier,
    ) {
        content()
    }
}

object ListAnimationDefaults {
    val itemFadeIn = fadeIn(tween(300))
    val itemFadeOut = fadeOut(tween(200))
    val itemSlideIn = slideInVertically(
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
    ) { it / 3 }
    val itemSlideOut = slideOutVertically(tween(200)) { -it / 3 }
    val itemExpand = expandVertically(
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
    )
    val itemShrink = shrinkVertically(
        animationSpec = tween(200),
    )
}

@Composable
fun <T> AnimatedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    staggered: Boolean = true,
    key: ((T) -> Any)? = null,
    emptyContent: @Composable () -> Unit = {},
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    if (items.isEmpty()) {
        emptyContent()
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> key?.invoke(item) ?: index },
        ) { index, item ->
            if (staggered) {
                StaggeredListItem(index = index) {
                    itemContent(index, item)
                }
            } else {
                itemContent(index, item)
            }
        }
    }
}

@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val pullDistance by animateDpAsState(
        targetValue = if (isRefreshing) 60.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "pullDistance",
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pullDistance),
            contentAlignment = Alignment.Center,
        ) {
            if (pullDistance > 0.dp) {
                PullIndicator(
                    progress = (pullDistance / 80.dp).coerceIn(0f, 1f),
                    isRefreshing = isRefreshing,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectPullToRefresh(
                        onRefresh = { onRefresh() },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun PullIndicator(
    progress: Float,
    isRefreshing: Boolean,
) {
    val rotation by animateDpAsState(
        targetValue = if (isRefreshing) 360.dp else (progress * 360).dp,
        animationSpec = if (isRefreshing) {
            spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
        } else spring(),
        label = "pullRotation",
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f + progress * 0.8f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

private suspend fun detectPullToRefresh(
    onRefresh: () -> Unit,
) {
    onRefresh()
}

@Composable
fun InfiniteScrollHandler(
    listState: LazyListState,
    buffer: Int = 3,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - buffer
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { needsMore ->
                if (needsMore) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun AnimatedSectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else (-90).dp,
        animationSpec = spring(),
        label = "arrowRotation",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
