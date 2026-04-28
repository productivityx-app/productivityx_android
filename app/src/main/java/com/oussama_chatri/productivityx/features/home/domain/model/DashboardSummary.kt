package com.oussama_chatri.productivityx.features.home.domain.model

import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task

data class DashboardSummary(
    val firstName: String,
    val tasksDueToday: Int,
    val tasksOverdue: Int,
    val totalActiveNotes: Int,
    val dueTodayTasks: List<Task>,
    val upcomingEvents: List<Event>,
    val recentNotes: List<Note>,
    val todayFocusMinutes: Int,
    val completedSessionsToday: Int,
)
