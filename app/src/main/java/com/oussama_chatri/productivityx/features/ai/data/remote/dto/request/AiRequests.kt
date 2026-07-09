package com.oussama_chatri.productivityx.features.ai.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val title: String? = null,
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val model: String? = null,
    val contextEnabled: Boolean? = null
)
