package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.navigation.MainRoute
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

enum class NavLabelBehavior { ALWAYS_SHOW, ACTIVE_ONLY, HIDE }

data class BadgeData(
    val count: Int = 0,
    val showDot: Boolean = false,
)

data class NavItemConfig(
    val labelRes: Int,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
    val route: MainRoute,
    val badge: BadgeData = BadgeData(),
    val shortcutActions: List<NavShortcutAction> = emptyList(),
)

data class NavShortcutAction(
    val labelRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

private val defaultNavItems: List<NavItemConfig> = listOf(
    NavItemConfig(R.string.nav_home, Icons.Outlined.Home, Icons.Outlined.Home, MainRoute.Home),
    NavItemConfig(R.string.nav_notes, Icons.Outlined.StickyNote2, Icons.Filled.StickyNote2, MainRoute.Notes),
    NavItemConfig(R.string.nav_tasks, Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle, MainRoute.Tasks),
    NavItemConfig(R.string.nav_events, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth, MainRoute.Calendar),
    NavItemConfig(R.string.nav_pomodoro, Icons.Outlined.Timer, Icons.Filled.Timer, MainRoute.Pomodoro),
)

@Composable
fun PxBottomNavBar(
    currentRoute: String?,
    onNavItemClick: (MainRoute) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItemConfig> = defaultNavItems,
    labelBehavior: NavLabelBehavior = NavLabelBehavior.ALWAYS_SHOW,
    floatingVariant: Boolean = true,
    visible: Boolean = true,
    selectedIndex: Int = items.indexOfFirst { it.route::class.qualifiedName == currentRoute }.coerceAtLeast(0),
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (floatingVariant) Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = PxColors.Primary.copy(alpha = 0.25f))
                    else Modifier
                ),
            shape = if (floatingVariant) RoundedCornerShape(32.dp) else RoundedCornerShape(0.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (floatingVariant) 68.dp else 72.dp)
                    .then(
                        if (!floatingVariant) Modifier
                            .drawBehind {
                                drawRoundRect(
                                    color = PxColors.Primary.copy(alpha = 0.18f),
                                    size = size.copy(height = 1.dp.toPx()),
                                    cornerRadius = CornerRadius.Zero,
                                )
                            }
                            .background(PxColors.Surface)
                        else Modifier.background(PxColors.Surface.copy(alpha = 0.85f), RoundedCornerShape(32.dp))
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = item.route::class.qualifiedName == currentRoute
                        NavBarTab(
                            item = item,
                            isSelected = isSelected,
                            labelBehavior = labelBehavior,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavItemClick(item.route)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavBarTab(
    item: NavItemConfig,
    isSelected: Boolean,
    labelBehavior: NavLabelBehavior,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabIconTint",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabLabelColor",
    )
    val pillBg by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabPillBg",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (item.shortcutActions.isNotEmpty()) showMenu = true
                    else onClick()
                },
            )
            .padding(vertical = 4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(pillBg)
                    .padding(horizontal = 14.dp, vertical = 3.dp),
            ) {
                Icon(
                    imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                    contentDescription = stringResource(item.labelRes),
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            if (labelBehavior != NavLabelBehavior.HIDE) {
                if (labelBehavior == NavLabelBehavior.ALWAYS_SHOW || isSelected) {
                    Text(
                        text = stringResource(item.labelRes),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (item.badge.count > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(PxColors.Error),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.badge.count.toString(),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            item.shortcutActions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(action.labelRes)) },
                    onClick = {
                        showMenu = false
                        action.onClick()
                    },
                    leadingIcon = {
                        Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}
