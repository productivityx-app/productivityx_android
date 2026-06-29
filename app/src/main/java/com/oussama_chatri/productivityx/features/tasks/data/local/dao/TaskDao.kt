package com.oussama_chatri.productivityx.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun observeTopLevelTasks(userId: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun getPagedTopLevelTasks(userId: String): PagingSource<Int, TaskEntity>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND status = :status
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun observeTopLevelTasksByStatus(userId: String, status: TaskStatus): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND status = :status
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun getPagedTopLevelTasksByStatus(userId: String, status: TaskStatus): PagingSource<Int, TaskEntity>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND priority = :priority
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun observeTopLevelTasksByPriority(userId: String, priority: Priority): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND priority = :priority
        ORDER BY position ASC, updated_at DESC
    """
    )
    fun getPagedTopLevelTasksByPriority(userId: String, priority: Priority): PagingSource<Int, TaskEntity>

    @Query(
        """
        SELECT * FROM tasks
        WHERE parent_task_id = :parentTaskId
          AND is_deleted = 0
        ORDER BY position ASC, created_at ASC
    """
    )
    fun observeSubtasks(parentTaskId: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 1
          AND parent_task_id IS NULL
        ORDER BY deleted_at DESC
    """
    )
    fun observeTrash(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): TaskEntity?

    @Query(
        """
        SELECT * FROM tasks
        WHERE parent_task_id = :parentId AND is_deleted = 0
        ORDER BY position ASC, created_at ASC
    """
    )
    suspend fun getSubtasksByParentId(parentId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET position = :position WHERE id = :taskId")
    suspend fun updatePosition(taskId: String, position: Int)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    // AI context queries

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND status NOT IN ('DONE', 'CANCELLED')
          AND due_date = :today
    """
    )
    suspend fun countDueToday(userId: String, today: LocalDate): Int

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND status NOT IN ('DONE', 'CANCELLED')
          AND due_date < :today
    """
    )
    suspend fun countOverdue(userId: String, today: LocalDate): Int

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND status NOT IN ('DONE', 'CANCELLED')
    """
    )
    suspend fun countActive(userId: String): Int

    @Query(
        """
        SELECT * FROM tasks
        WHERE user_id = :userId AND is_deleted = 0
          AND (LOWER(title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(description) LIKE '%' || LOWER(:query) || '%')
        ORDER BY updated_at DESC
    """
    )
    suspend fun searchTasks(userId: String, query: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE is_deleted = 0")
    suspend fun getAllNonDeleted(): List<TaskEntity>
}