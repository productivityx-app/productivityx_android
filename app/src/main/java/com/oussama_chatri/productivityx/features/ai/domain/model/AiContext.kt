package com.oussama_chatri.productivityx.features.ai.domain.model

data class AiContext(
    val tasksDueToday: Int,
    val tasksOverdue: Int,
    val totalActiveTasks: Int,
    val upcomingEventsThisWeek: Int,
    val lastEditedNoteTitle: String?,
    val currentPomodoroTask: String?,
    val todayFocusMinutes: Int,
)
