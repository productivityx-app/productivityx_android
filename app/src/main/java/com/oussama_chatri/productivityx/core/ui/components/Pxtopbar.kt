package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import kotlin.math.min

enum class TopBarStyle { STANDARD, LARGE }
enum class NavIconMode { MENU, BACK, CLOSE, NONE }

data class FilterChipData(
    val id: String,
    val label: String,
    val isSelected: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PxTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    style: TopBarStyle = TopBarStyle.STANDARD,
    scrollFraction: Float = 0f,
    showProgress: Boolean = false,
    progressValue: Float = 0f,
    filterChips: List<FilterChipData> = emptyList(),
    onFilterChipClick: ((String) -> Unit)? = null,
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (scrollFraction > 0.05f) 1f else if (style == TopBarStyle.LARGE) 0f else 1f,
        animationSpec = spring(),
        label = "topBarBgAlpha",
    )

    Column(modifier = modifier) {
        if (style == TopBarStyle.LARGE) {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PxColors.OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PxColors.OnSurfaceDim,
                                maxLines = 1,
                            )
                        }
                    }
                },
                navigationIcon = navigationIcon ?: {},
                actions = { actions() },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = PxColors.Background.copy(alpha = bgAlpha),
                    scrolledContainerColor = PxColors.Background,
                    titleContentColor = PxColors.OnBackground,
                    actionIconContentColor = PxColors.OnSurface,
                    navigationIconContentColor = PxColors.OnSurface,
                ),
            )
        } else {
            val titleScale by animateFloatAsState(
                targetValue = if (scrollFraction > 0.05f) 0.85f else 1f,
                animationSpec = spring(),
                label = "titleScale",
            )

            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PxColors.OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .scale(titleScale)
                                .alpha(if (scrollFraction > 0.05f) 0.85f else 1f),
                        )
                        if (subtitle != null && scrollFraction < 0.05f) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = PxColors.OnSurfaceDim,
                                maxLines = 1,
                            )
                        }
                    }
                },
                navigationIcon = navigationIcon ?: {},
                actions = { actions() },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PxColors.Background.copy(alpha = bgAlpha),
                    scrolledContainerColor = PxColors.Background,
                    titleContentColor = PxColors.OnBackground,
                    actionIconContentColor = PxColors.OnSurface,
                    navigationIconContentColor = PxColors.OnSurface,
                ),
            )
        }

        if (showProgress) {
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth(),
                color = PxColors.Primary,
                trackColor = PxColors.Primary.copy(alpha = 0.1f),
            )
        }

        if (filterChips.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filterChips) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (chip.isSelected) PxColors.Primary
                                else PxColors.SurfaceVariant
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onFilterChipClick?.invoke(chip.id)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (chip.isSelected) Color.White else PxColors.OnSurfaceDim,
                        )
                    }
                }
            }
        }

        val accentAlpha = (0.35f * min(scrollFraction * 3f, 1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .drawBehind {
                    drawRect(
                        color = PxColors.Primary.copy(alpha = accentAlpha),
                    )
                }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PxColors.Primary.copy(alpha = 0.0f),
                            PxColors.Primary.copy(alpha = accentAlpha),
                            PxColors.Secondary.copy(alpha = accentAlpha),
                            PxColors.Secondary.copy(alpha = 0.0f),
                        )
                    ),
                ),
        )
    }
}

@Composable
fun PxContextualTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIconMode: NavIconMode = NavIconMode.BACK,
    onNavClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    val navIcon: @Composable (() -> Unit)? = when (navigationIconMode) {
        NavIconMode.MENU -> {
            {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PxColors.OnSurface)
                }
            }
        }
        NavIconMode.BACK -> {
            {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PxColors.OnSurface)
                }
            }
        }
        NavIconMode.CLOSE -> {
            {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = PxColors.OnSurface)
                }
            }
        }
        NavIconMode.NONE -> null
    }

    PxTopBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = navIcon,
        actions = actions,
        modifier = modifier,
    )
}
