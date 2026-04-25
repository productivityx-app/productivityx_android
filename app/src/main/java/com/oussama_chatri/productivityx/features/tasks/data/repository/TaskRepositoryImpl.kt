package com.oussama_chatri.productivityx.features.tasks.data.repository

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import com.oussama_chatri.productivityx.features.tasks.data.remote.api.TaskApi
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.ReorderItemDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.ReorderRequestDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.TaskRequestDto
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.UpdateStatusRequestDto
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val prefsDataStore: PreferencesDataStore
) : TaskRepository {

    override fun observeTasks(status: TaskStatus?, priority: Priority?): Flow<List<Task>> {
        val userId = runCatching {
            kotlinx.coroutines.runBlocking { prefsDataStore.cachedUserId.first() ?: "" }
        }.getOrDefault("")

        val topLevelFlow = when {
            status != null -> taskDao.observeTopLevelTasksByStatus(userId, status)
            priority != null -> taskDao.observeTopLevelTasksByPriority(userId, priority)
            else -> taskDao.observeTopLevelTasks(userId)
        }

        return topLevelFlow.map { entities ->
            entities.map { entity ->
                // Eagerly load subtasks for each top-level task
                val subtaskEntities = taskDao.getSubtasksByParentId(entity.id)
                entity.toDomain(subtaskEntities.map { it.toDomain() })
            }
        }
    }

    override fun observeSubtasks(parentTaskId: String): Flow<List<Task>> =
        taskDao.observeSubtasks(parentTaskId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeTrash(): Flow<List<Task>> {
        val userId = runCatching {
            kotlinx.coroutines.runBlocking { prefsDataStore.cachedUserId.first() ?: "" }
        }.getOrDefault("")
        return taskDao.observeTrash(userId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTaskById(taskId: String): Resource<Task> {
        val local = taskDao.getById(taskId)
        if (local != null) {
            val subtasks = taskDao.getSubtasksByParentId(taskId)
            return Resource.Success(local.toDomain(subtasks.map { it.toDomain() }))
        }
        return when (val result = safeApiCall { taskApi.getTaskById(taskId) }) {
            is Resource.Success -> {
                val dto = result.data.data ?: return Resource.Error("Empty response")
                val entity = dto.toEntity(fallbackUserId = cachedUserId())
                taskDao.insert(entity)
                dto.subtasks?.forEach { taskDao.insert(it.toEntity()) }
                Resource.Success(dto.toDomain())
            }
            is Resource.Error -> Resource.Error(result.message, result.code)
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun createTask(
        title: String,
        description: String?,
        status: TaskStatus?,
        priority: Priority?,
        dueDate: LocalDate?,
        dueTime: LocalTime?,
        reminderAt: Instant?,
        estimatedMinutes: Int?,
        parentTaskId: String?,
        linkedEventId: String?
    ): Resource<Task> {
        val now = Instant.now()
        val userId = prefsDataStore.cachedUserId.first() ?: return Resource.Error("Not logged in")
        val tempId = UUID.randomUUID().toString()

        // Write to local DB immediately — offline-first
        val entity = TaskEntity(
            id = tempId,
            userId = userId,
            parentTaskId = parentTaskId,
            linkedEventId = linkedEventId,
            title = title,
            description = description,
            status = status ?: TaskStatus.TODO,
            priority = priority ?: Priority.MEDIUM,
            dueDate = dueDate,
            dueTime = dueTime,
            reminderAt = reminderAt,
            estimatedMinutes = estimatedMinutes,
            actualMinutes = 0,
            completedAt = null,
            position = 0,
            isDeleted = false,
            deletedAt = null,
            version = 1,
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.CREATE,
            createdAt = now,
            updatedAt = now
        )
        taskDao.insert(entity)

        val requestDto = TaskRequestDto(
            title = title,
            description = description,
            status = status?.name,
            priority = priority?.name,
            dueDate = dueDate?.toString(),
            dueTime = dueTime?.toString(),
            reminderAt = reminderAt?.toString(),
            estimatedMinutes = estimatedMinutes,
            parentTaskId = parentTaskId,
            linkedEventId = linkedEventId
        )

        return when (val result = safeApiCall { taskApi.createTask(requestDto) }) {
            is Resource.Success -> {
                val serverDto = result.data.data ?: return Resource.Error("Empty response")
                // Replace temp local entity with server response
                taskDao.deleteById(tempId)
                taskDao.insert(serverDto.toEntity(fallbackUserId = cachedUserId()))
                Resource.Success(serverDto.toDomain())
            }
            is Resource.Error -> {
                // Keep the local PENDING entry — SyncWorker will retry
                Resource.Success(entity.toDomain())
            }
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun updateTask(
        taskId: String,
        title: String?,
        description: String?,
        status: TaskStatus?,
        priority: Priority?,
        dueDate: LocalDate?,
        dueTime: LocalTime?,
        reminderAt: Instant?,
        estimatedMinutes: Int?,
        linkedEventId: String?
    ): Resource<Task> {
        val existing = taskDao.getById(taskId) ?: return Resource.Error("Task not found")
        val updated = existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            status = status ?: existing.status,
            priority = priority ?: existing.priority,
            dueDate = dueDate ?: existing.dueDate,
            dueTime = dueTime ?: existing.dueTime,
            reminderAt = reminderAt ?: existing.reminderAt,
            estimatedMinutes = estimatedMinutes ?: existing.estimatedMinutes,
            linkedEventId = linkedEventId ?: existing.linkedEventId,
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.UPDATE,
            updatedAt = Instant.now()
        )
        taskDao.update(updated)

        val requestDto = TaskRequestDto(
            title = updated.title,
            description = updated.description,
            status = updated.status.name,
            priority = updated.priority.name,
            dueDate = updated.dueDate?.toString(),
            dueTime = updated.dueTime?.toString(),
            reminderAt = updated.reminderAt?.toString(),
            estimatedMinutes = updated.estimatedMinutes,
            linkedEventId = updated.linkedEventId
        )

        return when (val result = safeApiCall { taskApi.updateTask(taskId, requestDto) }) {
            is Resource.Success -> {
                val dto = result.data.data ?: return Resource.Error("Empty response")
                taskDao.insert(dto.toEntity(fallbackUserId = cachedUserId()))
                Resource.Success(dto.toDomain())
            }
            is Resource.Error -> Resource.Success(updated.toDomain())
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun updateStatus(taskId: String, status: TaskStatus): Resource<Task> {
        val existing = taskDao.getById(taskId) ?: return Resource.Error("Task not found")
        val completedAt = if (status == TaskStatus.DONE && existing.completedAt == null) {
            Instant.now()
        } else if (status != TaskStatus.DONE) {
            null
        } else {
            existing.completedAt
        }

        val updated = existing.copy(
            status = status,
            completedAt = completedAt,
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.UPDATE,
            updatedAt = Instant.now()
        )
        taskDao.update(updated)

        return when (val result = safeApiCall {
            taskApi.updateStatus(taskId, UpdateStatusRequestDto(status.name))
        }) {
            is Resource.Success -> {
                val dto = result.data.data ?: return Resource.Error("Empty response")
                taskDao.insert(dto.toEntity(fallbackUserId = cachedUserId()))
                Resource.Success(dto.toDomain())
            }
            is Resource.Error -> Resource.Success(updated.toDomain())
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun reorder(items: List<Pair<String, Int>>): Resource<Unit> {
        items.forEach { (id, position) -> taskDao.updatePosition(id, position) }

        val requestDto = ReorderRequestDto(
            items = items.map { (id, pos) -> ReorderItemDto(id, pos) }
        )
        return when (val result = safeApiCall { taskApi.reorder(requestDto) }) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message, result.code)
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun softDeleteTask(taskId: String): Resource<Task> {
        val existing = taskDao.getById(taskId) ?: return Resource.Error("Task not found")
        val updated = existing.copy(
            isDeleted = true,
            deletedAt = Instant.now(),
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.UPDATE,
            updatedAt = Instant.now()
        )
        taskDao.update(updated)

        return when (val result = safeApiCall { taskApi.softDeleteTask(taskId) }) {
            is Resource.Success -> {
                val dto = result.data.data ?: return Resource.Error("Empty response")
                taskDao.insert(dto.toEntity(fallbackUserId = cachedUserId()))
                Resource.Success(dto.toDomain())
            }
            is Resource.Error -> Resource.Success(updated.toDomain())
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun restoreTask(taskId: String): Resource<Task> {
        val existing = taskDao.getById(taskId) ?: return Resource.Error("Task not found")
        val updated = existing.copy(
            isDeleted = false,
            deletedAt = null,
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.UPDATE,
            updatedAt = Instant.now()
        )
        taskDao.update(updated)

        return when (val result = safeApiCall { taskApi.restoreTask(taskId) }) {
            is Resource.Success -> {
                val dto = result.data.data ?: return Resource.Error("Empty response")
                taskDao.insert(dto.toEntity(fallbackUserId = cachedUserId()))
                Resource.Success(dto.toDomain())
            }
            is Resource.Error -> Resource.Success(updated.toDomain())
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun hardDeleteTask(taskId: String): Resource<Unit> {
        taskDao.deleteById(taskId)
        return when (val result = safeApiCall { taskApi.hardDeleteTask(taskId) }) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message, result.code)
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun refreshTasks(): Resource<Unit> {
        return when (val result = safeApiCall { taskApi.listTasks(size = 200) }) {
            is Resource.Success -> {
                val tasks = result.data.data?.content ?: return Resource.Error("Empty response")
                val userId = prefsDataStore.cachedUserId.first() ?: return Resource.Error("Not logged in")
                taskDao.deleteAllForUser(userId)
                taskDao.insertAll(tasks.map { it.toEntity() })
                Resource.Success(Unit)
            }
            is Resource.Error -> Resource.Error(result.message, result.code)
            Resource.Loading -> Resource.Loading
        }
    }

    private fun cachedUserId(): String =
        runCatching { kotlinx.coroutines.runBlocking { prefsDataStore.cachedUserId.first() ?: "" } }.getOrDefault("")

}
