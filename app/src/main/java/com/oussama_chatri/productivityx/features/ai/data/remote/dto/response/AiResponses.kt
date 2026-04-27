package com.oussama_chatri.productivityx.features.ai.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationResponse(
    val id: String,
    val title: String?,
    @SerialName("is_archived")    val isArchived: Boolean = false,
    @SerialName("last_message")   val lastMessage: String? = null,
    @SerialName("message_count")  val messageCount: Int = 0,
    @SerialName("created_at")     val createdAt: String,
    @SerialName("updated_at")     val updatedAt: String,
)

@Serializable
data class MessageResponse(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    val role: String,
    val content: String,
    @SerialName("action_block")    val actionBlock: String? = null,
    @SerialName("token_count")     val tokenCount: Int? = null,
    @SerialName("created_at")      val createdAt: String,
)

@Serializable
data class ConversationsListResponse(
    val conversations: List<ConversationResponse>,
)
