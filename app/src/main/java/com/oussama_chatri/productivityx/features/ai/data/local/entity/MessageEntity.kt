package com.oussama_chatri.productivityx.features.ai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oussama_chatri.productivityx.core.enums.MessageRole
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity        = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns  = ["conversation_id"],
            onDelete      = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("conversation_id")],
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID,

    @ColumnInfo(name = "conversation_id")
    val conversationId: UUID,

    @ColumnInfo(name = "role")
    val role: MessageRole,

    @ColumnInfo(name = "content")
    val content: String,

    // JSON-serialized AiActionBlock — null when no action present
    @ColumnInfo(name = "action_block_json")
    val actionBlockJson: String?,

    @ColumnInfo(name = "token_count")
    val tokenCount: Int?,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
)
