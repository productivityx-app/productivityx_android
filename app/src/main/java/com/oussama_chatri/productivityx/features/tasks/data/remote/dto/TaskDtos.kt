package com.oussama_chatri.productivityx.features.tasks.data.remote.dto

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// Request DTOs

@Serializable
data class TaskRequestDto(
    val title: String,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    @SerialName("dueDate") val dueDate: String? = null,
    @SerialName("dueTime") val dueTime: String? = null,
    @SerialName("reminderAt") val reminderAt: String? = null,
    @SerialName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerialName("parentTaskId") val parentTaskId: String? = null,
    @SerialName("linkedEventId") val linkedEventId: String? = null,
    val position: Int? = null
)

@Serializable
data class UpdateStatusRequestDto(
    val status: String
)

@Serializable
data class ReorderItemDto(
    val id: String,
    val position: Int
)

@Serializable
data class ReorderRequestDto(
    val items: List<ReorderItemDto>
)

// Response DTOs

@Serializable
data class PagedTaskResponseDto(
    val content: List<TaskResponseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean
)

@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
)

@Serializable
data class TaskResponseDto(
    val id: String,
    @SerialName("userId") val userId: String? = null,       // nullable
    @SerialName("parentTaskId") val parentTaskId: String? = null,
    @SerialName("linkedEventId") val linkedEventId: String? = null,
    val title: String,
    val description: String? = null,
    val status: String,
    val priority: String,
    @SerialName("dueDate") val dueDate: String? = null,
    @SerialName("dueTime") val dueTime: String? = null,
    @SerialName("reminderAt") val reminderAt: String? = null,
    @SerialName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerialName("actualMinutes") val actualMinutes: Int = 0,
    @SerialName("completedAt") val completedAt: String? = null,
    val position: Int = 0,
    val deleted: Boolean = false,
    @SerialName("deletedAt") val deletedAt: String? = null,
    val version: Int = 1,
    @SerialName("syncStatus") val syncStatus: String? = null,
    val subtasks: List<TaskResponseDto>? = null,
    @SerialName("createdAt") val createdAt: String? = null,  // nullable
    @SerialName("updatedAt") val updatedAt: String? = null   // nullable
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