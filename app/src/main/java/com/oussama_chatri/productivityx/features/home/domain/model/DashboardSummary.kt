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
    val tasksCompletedToday: Int = 0,
    val dailyQuote: String = "",
    val dailyQuoteAuthor: String = "",
    val widgetOrder: List<WidgetType> = WidgetType.defaultOrder(),
    val widgetVisibility: Map<WidgetType, Boolean> = emptyMap(),
    val widgetUsageCount: Map<WidgetType, Int> = emptyMap(),
    val isFocusMode: Boolean = false,
    val weatherTemp: String? = null,
    val weatherCondition: String? = null,
    val weatherIcon: String? = null,
    val totalEstimatedFocusMinutes: Int = 120,
)

enum class WidgetType(val defaultSpan: Int = 1) {
    GREETING(2),
    TODAYS_TASKS(1),
    UPCOMING_EVENTS(1),
    FOCUS_TIME(1),
    RECENT_NOTES(2),
    DAILY_QUOTE(1),
    AI_QUICK_ACTION(1),
    FOCUS_MODE_TOGGLE(1);

    companion object {
        fun defaultOrder() = listOf(
            GREETING,
            TODAYS_TASKS,
            UPCOMING_EVENTS,
            FOCUS_TIME,
            RECENT_NOTES,
            DAILY_QUOTE,
            AI_QUICK_ACTION,
            FOCUS_MODE_TOGGLE,
        )
    }
}
