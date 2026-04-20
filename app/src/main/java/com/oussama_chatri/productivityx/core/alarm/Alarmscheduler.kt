package com.oussama_chatri.productivityx.core.alarm

interface AlarmScheduler {
    fun scheduleTaskReminder(taskId: String, taskTitle: String, triggerAtMs: Long)
    fun scheduleEventReminder(eventId: String, eventTitle: String, triggerAtMs: Long)
    fun cancel(id: String)
}