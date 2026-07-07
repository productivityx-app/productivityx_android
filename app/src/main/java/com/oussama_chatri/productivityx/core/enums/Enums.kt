package com.oussama_chatri.productivityx.core.enums

enum class SyncStatus { PENDING, SYNCING, SYNCED, CONFLICT }

enum class SyncOperation { CREATE, UPDATE, DELETE }

enum class EntityType { NOTE, TASK, EVENT, PROFILE, PREFERENCES, POMODORO }

enum class Priority { LOW, MEDIUM, HIGH, URGENT }

enum class TaskStatus { TODO, IN_PROGRESS, ON_HOLD, DONE, CANCELLED }

enum class PomodoroType { FOCUS, SHORT_BREAK, LONG_BREAK }

enum class MessageRole { USER, ASSISTANT }

enum class AppTheme {
    DARK,
    LIGHT,
    SYSTEM
}
enum class Gender { MALE, FEMALE }

enum class TaskView { LIST, KANBAN, CALENDAR, TIMELINE }

enum class TaskSort { DUE_DATE, PRIORITY, CREATED_AT, TITLE }

enum class WeekStartDay { MON, SUN, SAT }

enum class RecurrenceType {
    NONE, DAILY, WEEKDAYS, WEEKLY, BIWEEKLY, MONTHLY, YEARLY, CUSTOM
}

enum class BadgeType {
    FIRST_TASK, TEN_TASKS, FIFTY_TASKS, HUNDRED_TASKS,
    WEEK_STREAK, TWO_WEEK_STREAK, MONTH_STREAK,
    COMPLETED_WEEK, COMPLETED_MONTH,
    EARLY_BIRD, NIGHT_OWL,
    CATEGORY_MASTER
}

data class Badge(
    val type: BadgeType,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long? = null
)