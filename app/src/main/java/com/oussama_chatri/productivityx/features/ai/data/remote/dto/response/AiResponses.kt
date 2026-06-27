package com.oussama_chatri.productivityx.features.ai.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ConversationResponse(
    val id: String,
    val userId: String? = null,
    val title: String?,
    val archived: Boolean = false,
    val messages: List<MessageResponse> = emptyList(),
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class MessageResponse(
    val id: String,
    @SerializedName("conversationId") val conversationId: String,
    val role: String,
    val content: String,
    @SerializedName("actionBlock") val actionBlock: String? = null,
    @SerializedName("tokenCount") val tokenCount: Int? = null,
    @SerializedName("createdAt") val createdAt: String,
)

data class ConversationsListResponse(
    val conversations: List<ConversationResponse>,
)
