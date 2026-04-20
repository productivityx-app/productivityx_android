package com.oussama_chatri.productivityx.core.notifications

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.oussama_chatri.productivityx.MainActivity
import com.oussama_chatri.productivityx.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)

    private fun launchIntent(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun buildPomodoroNotification(
        title: String,
        text: String,
        playPauseIntent: PendingIntent,
        skipIntent: PendingIntent,
        stopIntent: PendingIntent,
        isRunning: Boolean
    ): Notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_POMODORO)
        // FIX: Use android.R.drawable fallbacks so the project compiles before custom drawables
        // are added. Replace these with your actual drawable resources once they exist:
        //   R.drawable.ic_pomodoro, R.drawable.ic_pause, R.drawable.ic_play,
        //   R.drawable.ic_skip, R.drawable.ic_stop
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(launchIntent())
        .setOngoing(true)
        .setSilent(true)
        .addAction(
            if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isRunning) context.getString(R.string.pomodoro_pause)
            else context.getString(R.string.pomodoro_resume),
            playPauseIntent
        )
        .addAction(
            android.R.drawable.ic_media_next,
            context.getString(R.string.pomodoro_skip),
            skipIntent
        )
        .addAction(
            android.R.drawable.ic_delete,
            context.getString(R.string.pomodoro_stop),
            stopIntent
        )
        .build()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTaskReminder(id: Int, taskTitle: String) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_TASK_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_task_reminder_title))
            .setContentText(taskTitle)
            .setContentIntent(launchIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEventReminder(id: Int, eventTitle: String, timeLabel: String) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_EVENT_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_event_reminder_title))
            .setContentText("$eventTitle · $timeLabel")
            .setContentIntent(launchIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    fun cancel(id: Int) = manager.cancel(id)

    fun cancelAll() = manager.cancelAll()
}