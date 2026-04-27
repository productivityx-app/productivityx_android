package com.oussama_chatri.productivityx.features.ai.domain.model

import com.oussama_chatri.productivityx.core.enums.MessageRole
import java.time.Instant
import java.util.UUID

data class Message(
    val id: UUID,
    val conversationId: UUID,
    val role: MessageRole,
    val content: String,
    val actionBlock: AiActionBlock?,
    val tokenCount: Int?,
    val createdAt: Instant,
    // Transient — true only while SSE stream is active for this bubble
    val isStreaming: Boolean = false,
)
