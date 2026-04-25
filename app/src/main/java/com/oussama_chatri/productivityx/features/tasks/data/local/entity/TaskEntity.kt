package com.oussama_chatri.productivityx.features.tasks.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "parent_task_id") val parentTaskId: String?,
    @ColumnInfo(name = "linked_event_id") val linkedEventId: String?,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: Priority,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate?,
    @ColumnInfo(name = "due_time") val dueTime: LocalTime?,
    @ColumnInfo(name = "reminder_at") val reminderAt: Instant?,
    @ColumnInfo(name = "estimated_minutes") val estimatedMinutes: Int?,
    @ColumnInfo(name = "actual_minutes") val actualMinutes: Int = 0,
    @ColumnInfo(name = "completed_at") val completedAt: Instant?,
    val position: Int = 0,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
    val version: Int = 1,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @ColumnInfo(name = "pending_operation") val pendingOperation: SyncOperation? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
) {
    fun toDomain(subtasks: List<Task> = emptyList()): Task = Task(
        id = id,
        userId = userId,
        parentTaskId = parentTaskId,
        linkedEventId = linkedEventId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        dueDate = dueDate,
        dueTime = dueTime,
        reminderAt = reminderAt,
        estimatedMinutes = estimatedMinutes,
        actualMinutes = actualMinutes,
        completedAt = completedAt,
        position = position,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        version = version,
        syncStatus = syncStatus,
        subtasks = subtasks,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(task: Task, syncStatus: SyncStatus, pendingOp: SyncOperation?): TaskEntity =
            TaskEntity(
                id = task.id,
                userId = task.userId,
                parentTaskId = task.parentTaskId,
                linkedEventId = task.linkedEventId,
                title = task.title,
                description = task.description,
                status = task.status,
                priority = task.priority,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                reminderAt = task.reminderAt,
                estimatedMinutes = task.estimatedMinutes,
                actualMinutes = task.actualMinutes,
                completedAt = task.completedAt,
                position = task.position,
                isDeleted = task.isDeleted,
                deletedAt = task.deletedAt,
                version = task.version,
                syncStatus = syncStatus,
                pendingOperation = pendingOp,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
    }
}
