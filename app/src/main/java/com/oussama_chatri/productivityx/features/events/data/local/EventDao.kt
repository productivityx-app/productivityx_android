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

    // Returns the count of non-deleted events starting between now and end of the current week (AI context)
    @Query(
        """
        SELECT COUNT(*) FROM events
        WHERE userId = :userId
          AND isDeleted = 0
          AND startAt >= :weekStartMs
          AND startAt < :weekEndMs
    """
    )
    suspend fun countUpcomingThisWeek(userId: String, weekStartMs: Long, weekEndMs: Long): Int

    @Query(
        """
        SELECT * FROM events
        WHERE userId = :userId AND isDeleted = 0
          AND (LOWER(title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(description) LIKE '%' || LOWER(:query) || '%')
        ORDER BY updatedAt DESC
    """
    )
    suspend fun searchEvents(userId: String, query: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE isDeleted = 0")
    suspend fun getAllNonDeleted(): List<EventEntity>
}