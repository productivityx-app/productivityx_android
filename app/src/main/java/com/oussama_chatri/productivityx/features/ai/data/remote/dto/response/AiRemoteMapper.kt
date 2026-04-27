package com.oussama_chatri.productivityx.features.ai.data.remote.dto.response

import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.features.ai.data.local.entity.ConversationEntity
import com.oussama_chatri.productivityx.features.ai.data.local.entity.MessageEntity
import java.time.Instant
import java.util.UUID

fun ConversationResponse.toEntity() = ConversationEntity(
    id           = UUID.fromString(id),
    title        = title,
    isArchived   = isArchived,
    lastMessage  = lastMessage,
    messageCount = messageCount,
    createdAt    = Instant.parse(createdAt),
    updatedAt    = Instant.parse(updatedAt),
)

fun MessageResponse.toEntity() = MessageEntity(
    id              = UUID.fromString(id),
    conversationId  = UUID.fromString(conversationId),
    role            = MessageRole.valueOf(role.uppercase()),
    content         = content,
    actionBlockJson = actionBlock,
    tokenCount      = tokenCount,
    createdAt       = Instant.parse(createdAt),
)
