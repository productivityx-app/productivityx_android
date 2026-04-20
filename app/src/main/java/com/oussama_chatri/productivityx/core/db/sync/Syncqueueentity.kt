package com.oussama_chatri.productivityx.core.db.sync

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: EntityType,
    val entityId: String,
    val operation: SyncOperation,
    val payload: String,              // JSON snapshot of the entity at write time
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastAttemptedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePending(): Flow<List<SyncQueueEntity>>

    @Query("UPDATE sync_queue SET status = :status, retryCount = retryCount + 1, lastAttemptedAt = :attemptedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: SyncStatus, attemptedAt: Long)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sync_queue WHERE entityId = :entityId AND entityType = :type")
    suspend fun deleteByEntity(entityId: String, type: EntityType)

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>
}