package com.oussama_chatri.productivityx.features.ai.domain.model

import com.oussama_chatri.productivityx.core.enums.Priority

/**
 * Structured action parsed from an assistant message.
 * Rendered as a confirmation card before the client executes.
 */
sealed class AiActionBlock {

    data class CreateTask(
        val title: String,
        val priority: Priority?,
        val dueDate: String?,
    ) : AiActionBlock()

    data class CreateNote(
        val title: String,
        val content: String,
    ) : AiActionBlock()

    data class AddEvent(
        val title: String,
        val startAt: String,
        val durationMinutes: Int?,
    ) : AiActionBlock()
}
