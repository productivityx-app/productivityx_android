package com.oussama_chatri.productivityx.core.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class NotificationState {
    val notifications = mutableStateListOf<InAppNotification>()
    var showNotificationCenter by mutableStateOf(false)
    var currentToast by mutableStateOf<InAppNotification?>(null)

    val unreadCount: Int get() = notifications.count { !it.read }

    fun addNotification(
        title: String,
        body: String,
        type: NotificationType = NotificationType.SYSTEM_UPDATE,
        actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
        deepLink: String? = null,
    ) {
        val notif = InAppNotification(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            body = body,
            actionIcon = actionIcon,
            deepLink = deepLink,
        )
        notifications.add(0, notif)
        currentToast = notif
    }

    fun dismissToast() {
        currentToast = null
    }

    fun markAllRead() {
        val updated = notifications.map { it.copy(read = true) }
        notifications.clear()
        notifications.addAll(updated)
    }

    fun markRead(id: String) {
        val index = notifications.indexOfFirst { it.id == id }
        if (index >= 0) {
            notifications[index] = notifications[index].copy(read = true)
        }
    }
}

val LocalNotificationState = compositionLocalOf { NotificationState() }
