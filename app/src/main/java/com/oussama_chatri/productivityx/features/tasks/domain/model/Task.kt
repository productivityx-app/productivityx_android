package com.oussama_chatri.productivityx.features.tasks.domain.model

import com.oussama_chatri.productivityx.core.enums.Priority
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
    val estimatedMinutes: Int?,
    val actualMinutes: Int,
    val completedAt: Instant?,
    val position: Int,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val version: Int,
    val syncStatus: SyncStatus,
    val subtasks: List<Task>,
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

    val hasSubtasks: Boolean get() = subtasks.isNotEmpty()

    val isSubtask: Boolean get() = parentTaskId != null

}
