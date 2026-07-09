package com.oussama_chatri.productivityx.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.oussama_chatri.productivityx.core.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService<AlarmManager>()

    override fun scheduleTaskReminder(taskId: String, taskTitle: String, triggerAtMs: Long) {
        schedule(
            requestCode = taskId.hashCode(),
            intent = buildIntent(context, EXTRA_TYPE_TASK, taskId, taskTitle),
            triggerAtMs = triggerAtMs
        )
    }

    override fun scheduleEventReminder(eventId: String, eventTitle: String, triggerAtMs: Long) {
        schedule(
            requestCode = eventId.hashCode(),
            intent = buildIntent(context, EXTRA_TYPE_EVENT, eventId, eventTitle),
            triggerAtMs = triggerAtMs
        )
    }

    override fun cancel(id: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pending)
    }

    private fun schedule(requestCode: Int, intent: Intent, triggerAtMs: Long) {
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            alarmManager?.canScheduleExactAlarms() == false) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
        } else {
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
        }
    }

    private fun buildIntent(
        context: Context,
        type: String,
        entityId: String,
        title: String
    ): Intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(EXTRA_TYPE, type)
        putExtra(EXTRA_ENTITY_ID, entityId)
        putExtra(EXTRA_TITLE, title)
    }

    companion object {
        const val EXTRA_TYPE = "alarm_type"
        const val EXTRA_TYPE_TASK = "TASK"
        const val EXTRA_TYPE_EVENT = "EVENT"
        const val EXTRA_ENTITY_ID = "entity_id"
        const val EXTRA_TITLE = "entity_title"
    }
}

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_TYPE) ?: return
        val entityId = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_ENTITY_ID) ?: return
        val title = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_TITLE) ?: return

        val pendingResult = goAsync()
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                when (type) {
                    AlarmSchedulerImpl.EXTRA_TYPE_TASK ->
                        notificationHelper.showTaskReminder(entityId.hashCode(), title)
                    AlarmSchedulerImpl.EXTRA_TYPE_EVENT ->
                        notificationHelper.showEventReminder(entityId.hashCode(), title, "")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}