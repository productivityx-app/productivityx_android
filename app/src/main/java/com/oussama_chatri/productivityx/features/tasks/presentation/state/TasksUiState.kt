package com.oussama_chatri.productivityx.features.tasks.presentation.state

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.enums.TaskView
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.model.TaskFilter
import com.oussama_chatri.productivityx.features.tasks.domain.model.TaskSmartFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val activeTab: TaskTab = TaskTab.ALL,
    val viewMode: TaskView = TaskView.LIST,
    val filterStatus: TaskStatus? = null,
    val filterPriority: Priority? = null,
    val error: String? = null,
    val isSyncing: Boolean = false,

    // Multi-select
    val isMultiSelectMode: Boolean = false,
    val selectedTaskIds: Set<String> = emptySet(),

    // Smart filter
    val taskFilter: TaskFilter = TaskFilter(),
    val availableTags: List<String> = emptyList(),

    // Calendar view
    val calendarStartDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val calendarSelectedDate: LocalDate? = null,

    // Timeline view
    val timelineStartDate: LocalDate = LocalDate.now().minusDays(7),
    val timelineEndDate: LocalDate = LocalDate.now().plusDays(30),

    // Bulk action
    val isBulkActionRunning: Boolean = false
)

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val error: String? = null,
    val isEditingTitle: Boolean = false,
    val isEditingDescription: Boolean = false,
    val showCelebration: Boolean = false,
    val showConfetti: Boolean = false,
    val editingTitle: String = "",
    val editingDescription: String = ""
)

data class AddEditTaskUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderAt: Instant? = null,
    val reminderMinutes: Int? = null,
    val estimatedMinutes: Int? = null,
    val parentTaskId: String? = null,
    val linkedEventId: String? = null,
    val newSubtaskTitle: String = "",
    val subtasks: List<Task> = emptyList(),
    val tags: List<String> = emptyList(),
    val newTag: String = "",
    val recurrenceType: com.oussama_chatri.productivityx.core.enums.RecurrenceType = com.oussama_chatri.productivityx.core.enums.RecurrenceType.NONE,
    val recurrenceEndDate: LocalDate? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceDaysOfWeek: List<Int>? = null,
    val titleError: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showPriorityMatrix: Boolean = false
) {
    val isEditMode: Boolean get() = taskId != null
    val canSave: Boolean get() = title.isNotBlank()
}

data class TaskTrashUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val error: String? = null
)

data class TaskStatsUiState(
    val isLoading: Boolean = false,

    // Completion rate
    val weeklyCompletionRate: Float = 0f,
    val monthlyCompletionRate: Float = 0f,
    val weeklyCompletedCount: Int = 0,
    val weeklyTotalCount: Int = 0,
    val monthlyCompletedCount: Int = 0,
    val monthlyTotalCount: Int = 0,

    // Productivity
    val productivityScore: Float = 0f,
    val productivityTrend: Float = 0f, // positive or negative

    // Streak
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val daysWithTasks: List<LocalDate> = emptyList(),

    // Time per category
    val timePerPriority: Map<Priority, Int> = emptyMap(),

    // Achievements / Badges
    val badges: List<com.oussama_chatri.productivityx.core.enums.Badge> = emptyList(),
    val recentlyUnlockedBadge: com.oussama_chatri.productivityx.core.enums.Badge? = null,

    // Weekly review
    val weekCreatedCount: Int = 0,
    val weekCompletedCount: Int = 0,
    val weekOverdueCount: Int = 0,
    val weekSuggestedFocus: String = "",

    // Productivity patterns
    val hourlyHeatmap: Map<Int, Map<Int, Int>> = emptyMap(), // dayOfWeek -> hour -> count
    val categoryBreakdown: Map<Priority, Float> = emptyMap(),
    val completionVelocity: List<Pair<LocalDate, Int>> = emptyList()
)

enum class TaskTab { ALL, TODAY, UPCOMING, OVERDUE, COMPLETED }

val TaskTab.label: String get() = when (this) {
    TaskTab.ALL -> "All"
    TaskTab.TODAY -> "Today"
    TaskTab.UPCOMING -> "Upcoming"
    TaskTab.OVERDUE -> "Overdue"
    TaskTab.COMPLETED -> "Completed"
}

fun List<Task>.filterForTab(tab: TaskTab): List<Task> = when (tab) {
    TaskTab.ALL -> filter { !it.isDeleted && it.status != TaskStatus.CANCELLED }
    TaskTab.TODAY -> filter { it.isDueToday }
    TaskTab.UPCOMING -> filter {
        !it.isDeleted
                && it.dueDate != null
                && it.dueDate.isAfter(LocalDate.now())
                && it.status != TaskStatus.DONE
                && it.status != TaskStatus.CANCELLED
    }
    TaskTab.OVERDUE -> filter { it.isOverdue }
    TaskTab.COMPLETED -> filter { it.status == TaskStatus.DONE && !it.isDeleted }
}

fun List<Task>.filterBySmartFilter(filter: TaskSmartFilter): List<Task> = when (filter) {
    TaskSmartFilter.ALL -> filter { !it.isDeleted && it.status != TaskStatus.CANCELLED }
    TaskSmartFilter.TODAY -> filter { it.isDueToday }
    TaskSmartFilter.UPCOMING -> filter {
        !it.isDeleted && it.dueDate != null
                && it.dueDate.isAfter(LocalDate.now())
                && it.status != TaskStatus.DONE
                && it.status != TaskStatus.CANCELLED
    }
    TaskSmartFilter.OVERDUE -> filter { it.isOverdue }
    TaskSmartFilter.NO_DATE -> filter { it.dueDate == null && !it.isDeleted && it.status != TaskStatus.DONE && it.status != TaskStatus.CANCELLED }
    TaskSmartFilter.COMPLETED -> filter { it.status == TaskStatus.DONE && !it.isDeleted }
}

fun List<Task>.applyFilter(filter: TaskFilter): List<Task> {
    var result = this
    result = result.filterBySmartFilter(filter.smartFilter)
    filter.priorityFilter?.let { p -> result = result.filter { it.priority == p } }
    filter.statusFilter?.let { s -> result = result.filter { it.status == s } }
    filter.tagFilter?.let { tag -> result = result.filter { it.tags.contains(tag) } }
    if (filter.searchQuery.isNotBlank()) {
        val q = filter.searchQuery.lowercase()
        result = result.filter { it.title.lowercase().contains(q) || (it.description?.lowercase()?.contains(q) == true) }
    }
    filter.customStartDate?.let { start ->
        result = result.filter { it.dueDate != null && !it.dueDate.isBefore(start) }
    }
    filter.customEndDate?.let { end ->
        result = result.filter { it.dueDate != null && !it.dueDate.isAfter(end) }
    }
    return result
}

val TaskStatus.displayLabel: String get() = when (this) {
    TaskStatus.TODO -> "To Do"
    TaskStatus.IN_PROGRESS -> "In Progress"
    TaskStatus.ON_HOLD -> "On Hold"
    TaskStatus.DONE -> "Done"
    TaskStatus.CANCELLED -> "Cancelled"
}
