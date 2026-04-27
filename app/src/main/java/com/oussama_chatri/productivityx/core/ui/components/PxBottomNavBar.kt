package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

private data class NavItem(
    val label : String,
    val icon  : ImageVector,
    val route : MainRoute,
)

// 5 tabs per project spec: Home · Notes · Tasks · Pomodoro · AI
// Profile is reached via the avatar button in the Home top bar.
private val navItems = listOf(
    NavItem("Home",     Icons.Outlined.Home,        MainRoute.Home),
    NavItem("Notes",    Icons.Outlined.StickyNote2, MainRoute.Notes),
    NavItem("Tasks",    Icons.Outlined.CheckCircle, MainRoute.Tasks),
    NavItem("Pomodoro", Icons.Outlined.Timer,       MainRoute.Pomodoro),
    NavItem("AI",       Icons.Outlined.AutoAwesome, MainRoute.Ai),
)

@Composable
fun PxBottomNavBar(
    currentRoute   : String?,
    onNavItemClick : (MainRoute) -> Unit,
    modifier       : Modifier = Modifier,
) {
    val topBorderColor = PxColors.Primary.copy(alpha = 0.18f)

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind {
                // 1dp top accent line instead of a shadow
                drawRoundRect(
                    color        = topBorderColor,
                    size         = size.copy(height = 1.dp.toPx()),
                    cornerRadius = CornerRadius.Zero,
                )
            },
        containerColor = PxColors.Surface,
        tonalElevation = 0.dp,
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName

            val iconTint by animateColorAsState(
                targetValue   = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label         = "navIcon_${item.label}",
            )

            val labelColor by animateColorAsState(
                targetValue   = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                animationSpec = tween(200),
                label         = "navLabel_${item.label}",
            )

            val pillBg by animateColorAsState(
                targetValue   = if (isSelected) PxColors.Primary.copy(alpha = 0.14f) else Color.Transparent,
                animationSpec = spring(),
                label         = "navPill_${item.label}",
            )

            NavigationBarItem(
                selected = isSelected,
                onClick  = { onNavItemClick(item.route) },
                icon     = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(pillBg)
                                .padding(horizontal = 14.dp, vertical = 3.dp),
                        ) {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = item.label,
                                tint               = iconTint,
                                modifier           = Modifier.size(22.dp),
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text       = item.label,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = labelColor,
                    )
                },
                alwaysShowLabel = true,
                colors          = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxBottomNavBarPreview() {
    ProductivityXTheme {
        PxBottomNavBar(
            currentRoute   = MainRoute.Pomodoro::class.qualifiedName,
            onNavItemClick = {},
        )
    }
}