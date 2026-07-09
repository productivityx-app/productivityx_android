package com.oussama_chatri.productivityx.features.tasks.data.repository

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.alarm.AlarmScheduler
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.sync.SyncScheduler
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import com.oussama_chatri.productivityx.features.tasks.data.remote.api.TaskApi
import com.oussama_chatri.productivityx.features.tasks.data.remote.dto.TaskRequestDto
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.repository.TaskRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.flatMapLatest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val syncQueueDao: SyncQueueDao,
    private val syncScheduler: SyncScheduler,
    private val prefsDataStore: PreferencesDataStore,
    private val alarmScheduler: AlarmScheduler,
    private val gson: Gson
) : TaskRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeTasks(status: TaskStatus?, priority: Priority?): Flow<List<Task>> =
        prefsDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            val topLevelFlow = when {
                status != null -> taskDao.observeTopLevelTasksByStatus(userId, status)
                priority != null -> taskDao.observeTopLevelTasksByPriority(userId, priority)
                else -> taskDao.observeTopLevelTasks(userId)
            }

            topLevelFlow.map { entities ->
                entities.map { entity ->
                    // Eagerly load subtasks for each top-level task
                    val subtaskEntities = taskDao.getSubtasksByParentId(entity.id)
                    entity.toDomain(subtaskEntities.map { it.toDomain() })
                }
            }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getPagedTasks(status: TaskStatus?, priority: Priority?): Flow<PagingData<Task>> =
        prefsDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = true),
                pagingSourceFactory = {
                    when {
                        status != null -> taskDao.getPagedTopLevelTasksByStatus(userId, status)
                        priority != null -> taskDao.getPagedTopLevelTasksByPriority(userId, priority)
                        else -> taskDao.getPagedTopLevelTasks(userId)
                    }
                }
            ).flow
        }.map { pagingData ->
            pagingData.map { entity ->
                // PagingData.map takes a suspend lambda, so no runBlocking needed!
                val subtaskEntities = taskDao.getSubtasksByParentId(entity.id)
                entity.toDomain(subtaskEntities.map { it.toDomain() })
            }
        }

    override fun observeSubtasks(parentTaskId: String): Flow<List<Task>> =
        taskDao.observeSubtasks(parentTaskId).map { entities ->
            entities.map { it.toDomain() }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeTrash(): Flow<List<Task>> =
        prefsDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            taskDao.observeTrash(userId).map { entities -> entities.map { it.toDomain() } }
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
        if (reminderAt != null) {
            alarmScheduler.scheduleTaskReminder(tempId, title, reminderAt.toEpochMilli())
        }

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = tempId,
                operation = SyncOperation.CREATE,
                payload = gson.toJson(
                    TaskRequestDto(
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
                )
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(entity.toDomain())
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
        val effectiveReminderAt = reminderAt ?: existing.reminderAt
        if (effectiveReminderAt != null) {
            alarmScheduler.scheduleTaskReminder(taskId, updated.title, effectiveReminderAt.toEpochMilli())
        } else {
            alarmScheduler.cancel(taskId)
        }

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = taskId,
                operation = SyncOperation.UPDATE,
                payload = gson.toJson(
                    TaskRequestDto(
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
                )
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(updated.toDomain())
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

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = taskId,
                operation = SyncOperation.UPDATE,
                payload = gson.toJson(
                    TaskRequestDto(
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
                )
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(updated.toDomain())
    }

    override suspend fun reorder(items: List<Pair<String, Int>>): Resource<Unit> {
        val now = Instant.now()
        items.forEach { (id, position) ->
            taskDao.updatePosition(id, position)
            val entity = taskDao.getById(id)
            if (entity != null) {
                taskDao.update(entity.copy(
                    syncStatus = SyncStatus.PENDING,
                    pendingOperation = SyncOperation.UPDATE,
                    updatedAt = now
                ))
            }
        }

        if (isSyncEnabled()) {
            items.forEach { (id, position) ->
                val entity = taskDao.getById(id) ?: return@forEach
                enqueueSync(
                    entityId = id,
                    operation = SyncOperation.UPDATE,
                    payload = gson.toJson(
                        TaskRequestDto(
                            title = entity.title,
                            description = entity.description,
                            status = entity.status.name,
                            priority = entity.priority.name,
                            dueDate = entity.dueDate?.toString(),
                            dueTime = entity.dueTime?.toString(),
                            reminderAt = entity.reminderAt?.toString(),
                            estimatedMinutes = entity.estimatedMinutes,
                            parentTaskId = entity.parentTaskId,
                            linkedEventId = entity.linkedEventId,
                            position = position
                        )
                    )
                )
            }
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(Unit)
    }

    override suspend fun softDeleteTask(taskId: String): Resource<Task> {
        val existing = taskDao.getById(taskId) ?: return Resource.Error("Task not found")
        val updated = existing.copy(
            isDeleted = true,
            deletedAt = Instant.now(),
            syncStatus = SyncStatus.PENDING,
            pendingOperation = SyncOperation.DELETE,
            updatedAt = Instant.now()
        )
        taskDao.update(updated)
        alarmScheduler.cancel(taskId)

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = taskId,
                operation = SyncOperation.DELETE,
                payload = "{}"
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(updated.toDomain())
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

        if (isSyncEnabled()) {
            enqueueSync(
                entityId = taskId,
                operation = SyncOperation.UPDATE,
                payload = gson.toJson(
                    TaskRequestDto(
                        title = updated.title,
                        description = updated.description,
                        status = updated.status.name,
                        priority = updated.priority.name,
                        dueDate = updated.dueDate?.toString(),
                        dueTime = updated.dueTime?.toString(),
                        reminderAt = updated.reminderAt?.toString(),
                        estimatedMinutes = updated.estimatedMinutes,
                        parentTaskId = updated.parentTaskId,
                        linkedEventId = updated.linkedEventId
                    )
                )
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(updated.toDomain())
    }

    override suspend fun hardDeleteTask(taskId: String): Resource<Unit> {
        taskDao.deleteById(taskId)
        alarmScheduler.cancel(taskId)
        if (isSyncEnabled()) {
            enqueueSync(
                entityId = taskId,
                operation = SyncOperation.DELETE,
                payload = "{}"
            )
            syncScheduler.scheduleImmediateSync()
        }
        return Resource.Success(Unit)
    }

    override suspend fun refreshTasks(): Resource<Unit> {
        if (!isSyncEnabled()) return Resource.Success(Unit)
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

    private suspend fun enqueueSync(entityId: String, operation: SyncOperation, payload: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = EntityType.TASK,
                entityId = entityId,
                operation = operation,
                payload = payload
            )
        )
    }

    private suspend fun cachedUserId(): String =
        prefsDataStore.cachedUserId.first() ?: ""

    private suspend fun isSyncEnabled(): Boolean = prefsDataStore.isSyncEnabled()
}
