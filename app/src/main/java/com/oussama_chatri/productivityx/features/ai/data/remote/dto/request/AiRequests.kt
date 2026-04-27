package com.oussama_chatri.productivityx.features.ai.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val title: String? = null,
)

@Serializable
data class SendMessageRequest(
    val content: String,
    @SerialName("context") val context: AiContextRequest? = null,
)

@Serializable
data class AiContextRequest(
    @SerialName("tasks_due_today")         val tasksDueToday: Int,
    @SerialName("tasks_overdue")           val tasksOverdue: Int,
    @SerialName("total_active_tasks")      val totalActiveTasks: Int,
    @SerialName("upcoming_events_week")    val upcomingEventsThisWeek: Int,
    @SerialName("last_edited_note_title")  val lastEditedNoteTitle: String? = null,
    @SerialName("current_pomodoro_task")   val currentPomodoroTask: String? = null,
    @SerialName("today_focus_minutes")     val todayFocusMinutes: Int,
)
