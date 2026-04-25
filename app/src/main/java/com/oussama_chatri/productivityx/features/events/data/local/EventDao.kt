package com.oussama_chatri.productivityx.features.events.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT * FROM events
        WHERE userId = :userId
          AND isDeleted = 0
          AND startAt >= :fromMs
          AND startAt < :toMs
        ORDER BY startAt ASC
    """
    )
    fun observeEvents(userId: String, fromMs: Long, toMs: Long): Flow<List<EventEntity>>

    @Query(
        """
        SELECT * FROM events
        WHERE userId = :userId
          AND isDeleted = 0
          AND startAt >= :nowMs
        ORDER BY startAt ASC
        LIMIT :limit
    """
    )
    fun observeUpcomingEvents(userId: String, nowMs: Long, limit: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getEventByIdAndUser(id: String, userId: String): EventEntity?

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Query("UPDATE events SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query(
        """
        SELECT * FROM events
        WHERE userId = :userId
          AND isDeleted = 0
          AND startAt >= :fromMs
          AND startAt < :toMs
        ORDER BY startAt ASC
    """
    )
    suspend fun getEventsInRange(userId: String, fromMs: Long, toMs: Long): List<EventEntity>
}
