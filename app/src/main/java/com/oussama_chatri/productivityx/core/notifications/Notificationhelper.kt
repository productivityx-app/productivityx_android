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
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesDataStore
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
        .setSmallIcon(R.drawable.ic_pomodoro)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(launchIntent())
        .setOngoing(true)
        .setSilent(true)
        .addAction(
            if (isRunning) R.drawable.ic_pause else R.drawable.ic_play,
            if (isRunning) context.getString(R.string.pomodoro_pause)
            else context.getString(R.string.pomodoro_resume),
            playPauseIntent
        )
        .addAction(
            R.drawable.ic_skip,
            context.getString(R.string.pomodoro_skip),
            skipIntent
        )
        .addAction(
            R.drawable.ic_stop,
            context.getString(R.string.pomodoro_stop),
            stopIntent
        )
        .build()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun showTaskReminder(id: Int, taskTitle: String) {
        if (isQuietHours()) return
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
    suspend fun showEventReminder(id: Int, eventTitle: String, timeLabel: String) {
        if (isQuietHours()) return
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_EVENT_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_event_reminder_title))
            .setContentText("$eventTitle · $timeLabel")
            .setContentIntent(launchIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    @androidx.annotation.VisibleForTesting(otherwise = androidx.annotation.VisibleForTesting.PRIVATE)
    internal suspend fun isQuietHours(calendar: Calendar = Calendar.getInstance()): Boolean {
        val start = prefs.quietHoursStart.first()
        val end = prefs.quietHoursEnd.first()
        if (start == end) return false

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return if (start < end) {
            currentHour in start until end
        } else {
            currentHour >= start || currentHour < end
        }
    }

    fun cancel(id: Int) = manager.cancel(id)

    fun cancelAll() = manager.cancelAll()
}