package com.oussama_chatri.productivityx.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object NotificationChannels {

    const val CHANNEL_POMODORO = "px_pomodoro"
    const val CHANNEL_TASK_REMINDER = "px_task_reminders"
    const val CHANNEL_EVENT_REMINDER = "px_event_reminders"
    const val CHANNEL_SYNC = "px_sync"
    const val CHANNEL_GENERAL = "px_general"

    fun createAll(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_POMODORO,
                    "Focus Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Live Pomodoro session timer" },

                NotificationChannel(
                    CHANNEL_TASK_REMINDER,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reminders for upcoming tasks" },

                NotificationChannel(
                    CHANNEL_EVENT_REMINDER,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reminders for calendar events" },

                NotificationChannel(
                    CHANNEL_SYNC,
                    "Sync",
                    NotificationManager.IMPORTANCE_MIN
                ).apply { description = "Background sync status" },

                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General app notifications" }
            )
        )
    }
}