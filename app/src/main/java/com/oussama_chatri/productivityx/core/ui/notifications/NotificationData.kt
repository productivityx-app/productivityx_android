package com.oussama_chatri.productivityx.core.ui.notifications

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationType {
    TASK_REMINDER,
    EVENT_REMINDER,
    POMODORO_FINISHED,
    POMODORO_BREAK,
    NOTE_REMINDER,
    SYSTEM_UPDATE,
}

data class InAppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val actionLabel: String? = null,
    val actionIcon: ImageVector? = null,
    val onAction: (() -> Unit)? = null,
    val deepLink: String? = null,
)
