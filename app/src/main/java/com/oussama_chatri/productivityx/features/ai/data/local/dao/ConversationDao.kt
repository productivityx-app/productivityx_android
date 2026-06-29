package com.oussama_chatri.productivityx.features.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oussama_chatri.productivityx.features.ai.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun findById(id: UUID): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConversationEntity)

    @Update
    suspend fun update(entity: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("UPDATE conversations SET is_archived = 1, updated_at = :now WHERE id = :id")
    suspend fun archive(id: UUID, now: java.time.Instant)

    @Query("UPDATE conversations SET is_pinned = 1 WHERE id = :id")
    suspend fun pin(id: UUID)

    @Query("UPDATE conversations SET is_pinned = 0 WHERE id = :id")
    suspend fun unpin(id: UUID)

    @Query("""
        SELECT * FROM conversations
        WHERE is_archived = 0
        AND (title LIKE '%' || :query || '%' OR last_message LIKE '%' || :query || '%')
        ORDER BY is_pinned DESC, updated_at DESC
    """)
    fun search(query: String): Flow<List<ConversationEntity>>

    @Query("UPDATE conversations SET unread_count = unread_count + 1 WHERE id = :id")
    suspend fun incrementUnread(id: UUID)

    @Query("UPDATE conversations SET unread_count = 0 WHERE id = :id")
    suspend fun clearUnread(id: UUID)

    // Bump the summary fields after a new message lands
    @Query("""
        UPDATE conversations
        SET last_message   = :lastMessage,
            message_count  = message_count + 1,
            unread_count   = unread_count + 1,
            updated_at     = :now
        WHERE id = :id
    """)
    suspend fun refreshSummary(id: UUID, lastMessage: String, now: java.time.Instant)
}
