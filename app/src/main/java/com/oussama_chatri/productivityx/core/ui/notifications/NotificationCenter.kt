package com.oussama_chatri.productivityx.core.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@Composable
fun NotificationCenter(
    visible: Boolean,
    notifications: List<InAppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (InAppNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring()) + slideInVertically(spring()) { it },
        exit = fadeOut(spring()) + slideOutVertically(spring()) { it },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(PxColors.Surface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clickable(enabled = false) {}
                    .padding(top = 20.dp, bottom = 32.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PxColors.OnSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onMarkAllRead) {
                            Text("Mark all read", color = PxColors.Primary)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = PxColors.Primary.copy(alpha = 0.12f),
                )

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No notifications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PxColors.OnSurfaceDim,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp),
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationCenterItem(
                                notification = notif,
                                onClick = { onNotificationClick(notif) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCenterItem(
    notification: InAppNotification,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (notification.read) PxColors.Primary.copy(alpha = 0.1f)
                    else PxColors.Primary.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = notification.actionIcon ?: Icons.Outlined.Notifications,
                contentDescription = null,
                tint = if (notification.read) PxColors.Primary.copy(alpha = 0.5f) else PxColors.Primary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notification.read) PxColors.OnSurfaceDim else PxColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!notification.read) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PxColors.Primary),
                    )
                }
            }

            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = PxColors.Primary.copy(alpha = 0.06f),
    )
}
