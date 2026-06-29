package com.oussama_chatri.productivityx.features.ai.domain.model

import java.time.Instant
import java.util.UUID

data class Conversation(
    val id: UUID,
    val title: String?,
    val isArchived: Boolean,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessage: String?,
    val messageCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
