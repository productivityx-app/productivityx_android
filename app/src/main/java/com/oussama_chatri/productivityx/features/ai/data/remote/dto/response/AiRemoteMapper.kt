package com.oussama_chatri.productivityx.features.ai.data.remote.dto.response

import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.features.ai.data.local.entity.ConversationEntity
import com.oussama_chatri.productivityx.features.ai.data.local.entity.MessageEntity
import java.time.Instant
import java.util.UUID

fun ConversationResponse.toEntity() = ConversationEntity(
    id           = UUID.fromString(id),
    title        = title,
    isArchived   = archived,
    lastMessage  = messages.lastOrNull()?.content?.take(100),
    messageCount = messages.size,
    createdAt    = Instant.parse(createdAt),
    updatedAt    = Instant.parse(updatedAt),
)

fun ConversationResponse.toEntityWithMessages() = buildList {
    val conversationEntity = ConversationEntity(
        id           = UUID.fromString(id),
        title        = title,
        isArchived   = archived,
        lastMessage  = messages.lastOrNull()?.content?.take(100),
        messageCount = messages.size,
        createdAt    = Instant.parse(createdAt),
        updatedAt    = Instant.parse(updatedAt),
    )
    add(conversationEntity)
    addAll(messages.map { it.toEntity() })
}

fun MessageResponse.toEntity() = MessageEntity(
    id              = UUID.fromString(id),
    conversationId  = UUID.fromString(conversationId),
    role            = runCatching { MessageRole.valueOf(role.uppercase()) }.getOrDefault(MessageRole.USER),
    content         = content,
    actionBlockJson = actionBlock,
    tokenCount      = tokenCount,
    createdAt       = Instant.parse(createdAt),
)
