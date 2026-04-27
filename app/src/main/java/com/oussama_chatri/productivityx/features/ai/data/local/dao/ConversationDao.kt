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

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY updated_at DESC")
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

    // Bump the summary fields after a new message lands
    @Query("""
        UPDATE conversations
        SET last_message   = :lastMessage,
            message_count  = message_count + 1,
            updated_at     = :now
        WHERE id = :id
    """)
    suspend fun refreshSummary(id: UUID, lastMessage: String, now: java.time.Instant)
}
