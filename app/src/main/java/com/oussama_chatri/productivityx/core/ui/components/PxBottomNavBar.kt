package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: MainRoute
)

private val navItems = listOf(
    NavItem("Home",     Icons.Outlined.Home,          MainRoute.Home),
    NavItem("Notes",    Icons.Outlined.StickyNote2,   MainRoute.Notes),
    NavItem("Tasks",    Icons.Outlined.CheckCircle,   MainRoute.Tasks),
    NavItem("Calendar", Icons.Outlined.CalendarMonth, MainRoute.Calendar),
    NavItem("AI",       Icons.Outlined.AutoAwesome,   MainRoute.Ai)
)

@Composable
fun PxBottomNavBar(
    currentRoute: String?,
    onNavItemClick: (MainRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(80.dp),
        containerColor = PxColors.Surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route::class.qualifiedName

            val iconTint by animateColorAsState(
                targetValue = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                animationSpec = tween(200),
                label = "navIconTint"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavItemClick(item.route) },
                icon = {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .background(
                                if (isSelected) PxColors.Primary.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconTint
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PxBottomNavBarPreview() {
    ProductivityXTheme {
        PxBottomNavBar(
            currentRoute = MainRoute.Home::class.qualifiedName,
            onNavItemClick = {}
        )
    }
}