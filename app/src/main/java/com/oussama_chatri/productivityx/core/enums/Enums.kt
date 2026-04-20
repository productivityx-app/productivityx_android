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
    SYSTEM,
    OCEAN,
    AMBER,
    FOREST,
    ROSE
}
enum class Gender { MALE, FEMALE }

enum class TaskView { LIST, KANBAN }

enum class CalendarView { WEEK, MONTH }

enum class TaskSort { DUE_DATE, PRIORITY, CREATED_AT, TITLE }

enum class WeekStartDay { MON, SUN, SAT }