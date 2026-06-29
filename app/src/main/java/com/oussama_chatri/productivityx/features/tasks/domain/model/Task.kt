package com.oussama_chatri.productivityx.features.tasks.domain.model

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.RecurrenceType
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: String,
    val userId: String,
    val parentTaskId: String?,
    val linkedEventId: String?,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val reminderAt: Instant?,
    val reminderMinutes: Int? = null,
    val estimatedMinutes: Int?,
    val actualMinutes: Int,
    val completedAt: Instant?,
    val position: Int,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val version: Int,
    val syncStatus: SyncStatus,
    val subtasks: List<Task>,
    val tags: List<String> = emptyList(),
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndDate: LocalDate? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceDaysOfWeek: List<Int>? = null,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val assigneeAvatar: String? = null,
    val activityLog: List<ActivityEntry> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isOverdue: Boolean
        get() = dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != TaskStatus.DONE
                && status != TaskStatus.CANCELLED

    val isDueToday: Boolean
        get() = dueDate == LocalDate.now()
                && status != TaskStatus.DONE
                && status != TaskStatus.CANCELLED

    val subtaskCount: Int get() = subtasks.size

    val completedSubtaskCount: Int
        get() = subtasks.count { it.status == TaskStatus.DONE }

    val subtaskProgress: Float
        get() = if (subtasks.isEmpty()) 1f else completedSubtaskCount.toFloat() / subtaskCount

    val hasSubtasks: Boolean get() = subtasks.isNotEmpty()

    val isSubtask: Boolean get() = parentTaskId != null

    val isRecurring: Boolean get() = recurrenceType != RecurrenceType.NONE
}

data class ActivityEntry(
    val id: String,
    val action: String,
    val timestamp: Instant,
    val details: String? = null
)

data class TaskFilter(
    val smartFilter: TaskSmartFilter = TaskSmartFilter.ALL,
    val priorityFilter: Priority? = null,
    val statusFilter: TaskStatus? = null,
    val tagFilter: String? = null,
    val searchQuery: String = "",
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null
) {
    val isCustomFilter: Boolean get() = customStartDate != null || customEndDate != null
}

enum class TaskSmartFilter {
    ALL, TODAY, UPCOMING, OVERDUE, NO_DATE, COMPLETED
}
