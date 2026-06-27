package com.oussama_chatri.productivityx.features.tasks.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// Request DTOs

data class TaskRequestDto(
    val title: String,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("dueTime") val dueTime: String? = null,
    @SerializedName("reminderAt") val reminderAt: String? = null,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerializedName("parentTaskId") val parentTaskId: String? = null,
    @SerializedName("linkedEventId") val linkedEventId: String? = null,
    val position: Int? = null
)

data class UpdateStatusRequestDto(
    val status: String
)

data class ReorderItemDto(
    val id: String,
    val position: Int
)

data class ReorderRequestDto(
    val items: List<ReorderItemDto>
)

// Response DTOs

data class PagedTaskResponseDto(
    val content: List<TaskResponseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean
)

data class TaskResponseDto(
    val id: String,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("parentTaskId") val parentTaskId: String? = null,
    @SerializedName("linkedEventId") val linkedEventId: String? = null,
    val title: String,
    val description: String? = null,
    val status: String,
    val priority: String,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("dueTime") val dueTime: String? = null,
    @SerializedName("reminderAt") val reminderAt: String? = null,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerializedName("actualMinutes") val actualMinutes: Int = 0,
    @SerializedName("completedAt") val completedAt: String? = null,
    val position: Int = 0,
    val deleted: Boolean = false,
    @SerializedName("deletedAt") val deletedAt: String? = null,
    val version: Int = 1,
    @SerializedName("syncStatus") val syncStatus: String? = null,
    val subtasks: List<TaskResponseDto>? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toEntity(fallbackUserId: String = ""): TaskEntity = TaskEntity(
        id = id,
        userId = userId ?: fallbackUserId,
        parentTaskId = parentTaskId,
        linkedEventId = linkedEventId,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = Priority.valueOf(priority),
        dueDate = dueDate?.let { LocalDate.parse(it) },
        dueTime = dueTime?.let { LocalTime.parse(it) },
        reminderAt = reminderAt?.let { Instant.parse(it) },
        estimatedMinutes = estimatedMinutes,
        actualMinutes = actualMinutes,
        completedAt = completedAt?.let { Instant.parse(it) },
        position = position,
        isDeleted = deleted,
        deletedAt = deletedAt?.let { Instant.parse(it) },
        version = version,
        syncStatus = SyncStatus.SYNCED,
        pendingOperation = null,
        createdAt = createdAt?.let { Instant.parse(it) } ?: Instant.now(),
        updatedAt = updatedAt?.let { Instant.parse(it) } ?: Instant.now()
    )

    fun toDomain(): Task = toEntity().toDomain(
        subtasks = subtasks?.map { it.toDomain() } ?: emptyList()
    )
}
