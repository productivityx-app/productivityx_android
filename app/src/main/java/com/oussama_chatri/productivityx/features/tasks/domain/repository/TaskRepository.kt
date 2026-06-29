package com.oussama_chatri.productivityx.features.tasks.domain.repository

import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {

    fun observeTasks(
        status: TaskStatus? = null,
        priority: Priority? = null
    ): Flow<List<Task>>

    fun getPagedTasks(
        status: TaskStatus? = null,
        priority: Priority? = null
    ): Flow<PagingData<Task>>

    fun observeSubtasks(parentTaskId: String): Flow<List<Task>>

    fun observeTrash(): Flow<List<Task>>

    suspend fun getTaskById(taskId: String): Resource<Task>

    suspend fun createTask(
        title: String,
        description: String? = null,
        status: TaskStatus? = null,
        priority: Priority? = null,
        dueDate: LocalDate? = null,
        dueTime: java.time.LocalTime? = null,
        reminderAt: java.time.Instant? = null,
        estimatedMinutes: Int? = null,
        parentTaskId: String? = null,
        linkedEventId: String? = null
    ): Resource<Task>

    suspend fun updateTask(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: TaskStatus? = null,
        priority: Priority? = null,
        dueDate: LocalDate? = null,
        dueTime: java.time.LocalTime? = null,
        reminderAt: java.time.Instant? = null,
        estimatedMinutes: Int? = null,
        linkedEventId: String? = null
    ): Resource<Task>

    suspend fun updateStatus(taskId: String, status: TaskStatus): Resource<Task>

    suspend fun reorder(items: List<Pair<String, Int>>): Resource<Unit>

    suspend fun softDeleteTask(taskId: String): Resource<Task>

    suspend fun restoreTask(taskId: String): Resource<Task>

    suspend fun hardDeleteTask(taskId: String): Resource<Unit>

    suspend fun refreshTasks(): Resource<Unit>
}
