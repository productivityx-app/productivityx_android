package com.oussama_chatri.productivityx.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var taskDao: TaskDao
    @Inject lateinit var eventDao: EventDao
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        runBlocking {
            rescheduleTaskReminders()
            rescheduleEventReminders()
        }
    }

    private suspend fun rescheduleTaskReminders() {
        val now = Instant.now()
        taskDao.getAllNonDeleted().forEach { task ->
            val reminderAt = task.reminderAt
            if (reminderAt != null && reminderAt.toEpochMilli() > now.toEpochMilli()) {
                alarmScheduler.scheduleTaskReminder(task.id, task.title, reminderAt.toEpochMilli())
            }
        }
    }

    private suspend fun rescheduleEventReminders() {
        val nowMs = Instant.now().toEpochMilli()
        eventDao.getAllNonDeleted().forEach { event ->
            val reminderMinutes = event.reminderMinutes
            if (reminderMinutes != null && reminderMinutes > 0) {
                val triggerAtMs = event.startAt - (reminderMinutes * 60000L)
                if (triggerAtMs > nowMs) {
                    alarmScheduler.scheduleEventReminder(event.id, event.title, triggerAtMs)
                }
            }
        }
    }
}
