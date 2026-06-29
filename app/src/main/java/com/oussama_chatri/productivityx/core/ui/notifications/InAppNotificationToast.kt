package com.oussama_chatri.productivityx.core.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationToast(
    notification: InAppNotification?,
    onDismiss: () -> Unit,
    onClick: (InAppNotification) -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 3000L,
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(spring()) { -it } + fadeIn(spring()),
        exit = slideOutVertically(spring()) { -it } + fadeOut(spring()),
        modifier = modifier,
    ) {
        notification?.let { notif ->
            LaunchedEffect(notif.id) {
                delay(autoDismissMillis)
                onDismiss()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(PxColors.Surface, RoundedCornerShape(12.dp))
                    .clickable { onClick(notif) }
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = notif.actionIcon ?: Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = PxColors.Primary,
                        modifier = Modifier.size(24.dp),
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PxColors.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = notif.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurfaceDim,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (notif.deepLink != null) {
                        Text(
                            text = "OPEN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PxColors.Primary,
                        )
                    }
                }
            }
        }
    }
}
