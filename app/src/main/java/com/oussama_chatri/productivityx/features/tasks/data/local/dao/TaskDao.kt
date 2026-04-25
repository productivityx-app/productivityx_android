package com.oussama_chatri.productivityx.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
        ORDER BY position ASC, updated_at DESC
    """)
    fun observeTopLevelTasks(userId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND status = :status
        ORDER BY position ASC, updated_at DESC
    """)
    fun observeTopLevelTasksByStatus(userId: String, status: TaskStatus): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 0
          AND parent_task_id IS NULL
          AND priority = :priority
        ORDER BY position ASC, updated_at DESC
    """)
    fun observeTopLevelTasksByPriority(userId: String, priority: Priority): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE parent_task_id = :parentTaskId
          AND is_deleted = 0
        ORDER BY position ASC, created_at ASC
    """)
    fun observeSubtasks(parentTaskId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND is_deleted = 1
          AND parent_task_id IS NULL
        ORDER BY deleted_at DESC
    """)
    fun observeTrash(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): TaskEntity?

    @Query("""
        SELECT * FROM tasks
        WHERE parent_task_id = :parentId AND is_deleted = 0
        ORDER BY position ASC, created_at ASC
    """)
    suspend fun getSubtasksByParentId(parentId: String): List<TaskEntity>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
          AND sync_status = 'PENDING'
        ORDER BY created_at ASC
    """)
    suspend fun getPendingTasks(userId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET sync_status = :status WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: String, status: SyncStatus)

    @Query("UPDATE tasks SET position = :position WHERE id = :taskId")
    suspend fun updatePosition(taskId: String, position: Int)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
