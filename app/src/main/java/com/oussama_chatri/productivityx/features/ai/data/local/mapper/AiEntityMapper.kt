package com.oussama_chatri.productivityx.features.ai.data.local.mapper

import com.oussama_chatri.productivityx.features.ai.data.local.entity.ConversationEntity
import com.oussama_chatri.productivityx.features.ai.data.local.entity.MessageEntity
import com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock
import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import com.oussama_chatri.productivityx.core.enums.Priority
import org.json.JSONObject

fun ConversationEntity.toDomain() = Conversation(
    id           = id,
    title        = title,
    isArchived   = isArchived,
    lastMessage  = lastMessage,
    messageCount = messageCount,
    createdAt    = createdAt,
    updatedAt    = updatedAt,
)

fun Conversation.toEntity() = ConversationEntity(
    id           = id,
    title        = title,
    isArchived   = isArchived,
    lastMessage  = lastMessage,
    messageCount = messageCount,
    createdAt    = createdAt,
    updatedAt    = updatedAt,
)

fun MessageEntity.toDomain() = Message(
    id             = id,
    conversationId = conversationId,
    role           = role,
    content        = content,
    actionBlock    = actionBlockJson?.parseActionBlock(),
    tokenCount     = tokenCount,
    createdAt      = createdAt,
)

fun Message.toEntity() = MessageEntity(
    id              = id,
    conversationId  = conversationId,
    role            = role,
    content         = content,
    actionBlockJson = actionBlock?.toJson(),
    tokenCount      = tokenCount,
    createdAt       = createdAt,
)

// ─── JSON helpers ────────────────────────────────────────────────────────────

private fun String.parseActionBlock(): AiActionBlock? = runCatching {
    val obj    = JSONObject(this)
    val action = obj.getString("action")
    when (action) {
        "CREATE_TASK" -> AiActionBlock.CreateTask(
            title    = obj.getString("title"),
            priority = obj.optString("priority").takeIf { it.isNotBlank() }
                ?.let { runCatching { Priority.valueOf(it) }.getOrNull() },
            dueDate  = obj.optString("dueDate").takeIf { it.isNotBlank() },
        )
        "CREATE_NOTE" -> AiActionBlock.CreateNote(
            title   = obj.getString("title"),
            content = obj.getString("content"),
        )
        "ADD_EVENT"   -> AiActionBlock.AddEvent(
            title           = obj.getString("title"),
            startAt         = obj.getString("startAt"),
            durationMinutes = obj.optInt("durationMinutes", -1).takeIf { it > 0 },
        )
        else          -> null
    }
}.getOrNull()

private fun AiActionBlock.toJson(): String {
    val obj = JSONObject()
    when (this) {
        is AiActionBlock.CreateTask -> {
            obj.put("action", "CREATE_TASK")
            obj.put("title", title)
            priority?.let { obj.put("priority", it.name) }
            dueDate?.let { obj.put("dueDate", it) }
        }
        is AiActionBlock.CreateNote -> {
            obj.put("action", "CREATE_NOTE")
            obj.put("title", title)
            obj.put("content", content)
        }
        is AiActionBlock.AddEvent -> {
            obj.put("action", "ADD_EVENT")
            obj.put("title", title)
            obj.put("startAt", startAt)
            durationMinutes?.let { obj.put("durationMinutes", it) }
        }
    }
    return obj.toString()
}
