package com.oussama_chatri.productivityx.features.tasks.domain.usecase

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    operator fun invoke(
        status: TaskStatus? = null,
        priority: Priority? = null
    ): Flow<List<Task>> = repo.observeTasks(status, priority)
}

class ObserveSubtasksUseCase @Inject constructor(private val repo: TaskRepository) {
    operator fun invoke(parentTaskId: String): Flow<List<Task>> =
        repo.observeSubtasks(parentTaskId)
}

class ObserveTaskTrashUseCase @Inject constructor(private val repo: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repo.observeTrash()
}

class GetTaskByIdUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(taskId: String): Resource<Task> = repo.getTaskById(taskId)
}

class CreateTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        status: TaskStatus? = null,
        priority: Priority? = null,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        reminderAt: Instant? = null,
        estimatedMinutes: Int? = null,
        parentTaskId: String? = null,
        linkedEventId: String? = null
    ): Resource<Task> = repo.createTask(
        title, description, status, priority,
        dueDate, dueTime, reminderAt, estimatedMinutes,
        parentTaskId, linkedEventId
    )
}

class UpdateTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: TaskStatus? = null,
        priority: Priority? = null,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        reminderAt: Instant? = null,
        estimatedMinutes: Int? = null,
        linkedEventId: String? = null
    ): Resource<Task> = repo.updateTask(
        taskId, title, description, status, priority,
        dueDate, dueTime, reminderAt, estimatedMinutes, linkedEventId
    )
}

class UpdateTaskStatusUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(taskId: String, status: TaskStatus): Resource<Task> =
        repo.updateStatus(taskId, status)
}

class ReorderTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(items: List<Pair<String, Int>>): Resource<Unit> =
        repo.reorder(items)
}

class SoftDeleteTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(taskId: String): Resource<Task> = repo.softDeleteTask(taskId)
}

class RestoreTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(taskId: String): Resource<Task> = repo.restoreTask(taskId)
}

class HardDeleteTaskUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(taskId: String): Resource<Unit> = repo.hardDeleteTask(taskId)
}

class RefreshTasksUseCase @Inject constructor(private val repo: TaskRepository) {
    suspend operator fun invoke(): Resource<Unit> = repo.refreshTasks()
}
