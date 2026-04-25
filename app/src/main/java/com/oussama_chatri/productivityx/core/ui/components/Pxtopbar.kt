package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

/**
 * App-wide top bar.
 *
 * Renders a subtle gradient accent line below the bar to visually separate content
 * without relying on drop shadows. The line fades from Primary to Secondary, matching
 * the brand language used throughout the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PxTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = PxColors.OnBackground
                    )
                    if (subtitle != null) {
                        Text(
                            text  = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurfaceDim
                        )
                    }
                }
            },
            navigationIcon  = navigationIcon ?: {},
            actions         = { actions() },
            scrollBehavior  = scrollBehavior,
            colors          = TopAppBarDefaults.topAppBarColors(
                containerColor         = PxColors.Background,
                scrolledContainerColor = PxColors.Background,
                titleContentColor      = PxColors.OnBackground,
                actionIconContentColor = PxColors.OnSurface,
                navigationIconContentColor = PxColors.OnSurface
            )
        )

        // Gradient accent line — replaces the default divider/shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PxColors.Primary.copy(alpha = 0.0f),
                            PxColors.Primary.copy(alpha = 0.35f),
                            PxColors.Secondary.copy(alpha = 0.35f),
                            PxColors.Secondary.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}