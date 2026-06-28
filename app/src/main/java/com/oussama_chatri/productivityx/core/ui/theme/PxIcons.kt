package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PxIconSizes {
    val xs   = 12.dp
    val sm   = 16.dp
    val md   = 20.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val hero = 48.dp
}

object PxIcons {
    object Navigation {
        val Home       = Icons.Filled.Home
        val Back       = Icons.Filled.ArrowBack
        val Forward    = Icons.Filled.ArrowForward
        val Menu       = Icons.Filled.Menu
        val More       = Icons.Filled.MoreVert
        val Close      = Icons.Filled.Close
        val Drag       = Icons.Filled.DragHandle
        val ChevronL   = Icons.Filled.KeyboardArrowLeft
        val ChevronR   = Icons.Filled.KeyboardArrowRight
    }

    object Action {
        val Add       = Icons.Filled.Add
        val Edit      = Icons.Filled.Edit
        val Delete    = Icons.Filled.Delete
        val Archive   = Icons.Filled.Archive
        val Share     = Icons.Filled.Share
        val Search    = Icons.Filled.Search
        val Refresh   = Icons.Filled.Refresh
        val Settings  = Icons.Filled.Settings
        val Done      = Icons.Filled.Check
        val DoneAll   = Icons.Filled.DoneAll
        val Save      = Icons.Filled.Create
        val Cancel    = Icons.Filled.Close
    }

    object Content {
        val Note     = Icons.Outlined.Description
        val Task     = Icons.Outlined.Checklist
        val Calendar = Icons.Filled.CalendarMonth
        val Folder   = Icons.Outlined.Folder
        val Label    = Icons.Outlined.Label
        val Flag     = Icons.Outlined.Flag
        val Star     = Icons.Filled.Star
        val StarOutline = Icons.Outlined.Star
        val Bookmark = Icons.Outlined.BookmarkBorder
        val Inbox    = Icons.Outlined.Inbox
    }

    object Status {
        val Success     = Icons.Filled.CheckCircle
        val Info        = Icons.Filled.Info
        val Warning     = Icons.Filled.Info
        val Error       = Icons.Filled.Info
        val NotifOn     = Icons.Filled.Notifications
        val NotifOff    = Icons.Outlined.Notifications
        val Visibility  = Icons.Filled.Visibility
        val VisibilityOff = Icons.Filled.VisibilityOff
    }

    object Feature {
        val Pomodoro    = Icons.Filled.Timer
        val PomodoroOutline = Icons.Outlined.Timer
        val Ai          = Icons.Filled.Lightbulb
        val AiOutline   = Icons.Outlined.Lightbulb
        val Profile     = Icons.Filled.Person
        val ProfileOutline = Icons.Outlined.Person
        val Theme       = Icons.Filled.Palette
        val ThemeOutline = Icons.Outlined.Palette
        val Schedule    = Icons.Filled.Schedule
        val Timer       = Icons.Filled.Timer
        val Lock        = Icons.Filled.Lock
        val Email       = Icons.Filled.Email
        val Play        = Icons.Filled.PlayArrow
        val Favorite    = Icons.Filled.Favorite
        val Palette     = Icons.Filled.Palette
        val Search      = Icons.Outlined.Search
        val Settings    = Icons.Outlined.Settings
        val Notifications = Icons.Outlined.Notifications
        val Time        = Icons.Outlined.AccessTime
    }
}

fun Color.isLight(): Boolean = this.luminance() > 0.5f

fun Color.accessibleOnBackground(): Color {
    return if (this.isLight()) Color.Black else Color.White
}

@Composable
fun IconContainer(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = PxIconSizes.lg,
    tint: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
) {
    val touchTarget = 48.dp
    val containerModifier = if (onClick != null) {
        modifier
            .size(touchTarget)
            .clickable(onClick = onClick)
    } else {
        modifier.size(touchTarget)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            tint = tint,
        )
    }
}

@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    state: AnimatedIconState,
    modifier: Modifier = Modifier,
    size: Dp = PxIconSizes.lg,
    tint: Color = LocalContentColor.current,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "animatedIcon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = when (state) {
                AnimatedIconState.Loading -> Modifier
                    .size(size)
                    .rotate(rotation)
                else -> Modifier.size(size)
            },
            tint = when (state) {
                AnimatedIconState.Loading -> PxColors.Primary
                AnimatedIconState.Success -> PxColors.Success
                AnimatedIconState.Error   -> PxColors.Error
            },
        )
    }
}

enum class AnimatedIconState { Loading, Success, Error }

@Composable
fun IconBadge(
    count: Int,
    modifier: Modifier = Modifier,
    badgeColor: Color = PxColors.Error,
    textColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (count > 0) {
                Badge(
                    containerColor = badgeColor,
                    contentColor = textColor,
                ) {
                    val display = if (count > 99) "99+" else count.toString()
                    androidx.compose.material3.Text(
                        text = display,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        content = content,
    )
}
