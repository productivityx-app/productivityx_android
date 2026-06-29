package com.oussama_chatri.productivityx.features.ai.presentation.state

import com.oussama_chatri.productivityx.features.ai.domain.model.AiContext
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import java.util.UUID

data class AiUiState(
    val conversationId  : UUID?          = null,
    val messages        : List<Message>  = emptyList(),
    val inputText       : String         = "",
    val isStreaming     : Boolean         = false,
    val streamingContent: String          = "",
    val context         : AiContext?     = null,
    val isContextLoading: Boolean         = false,
    val error           : String?        = null,
    val pendingAction   : com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock? = null,
    val replyToMessage  : Message?       = null,
    val showEmojiReaction: UUID?         = null,
    val suggestions     : List<String>   = emptyList(),
    val personaType     : AiPersonaType  = AiPersonaType.PRODUCTIVITY,
    val isContextExpanded: Boolean       = true,
)

enum class AiPersonaType {
    PRODUCTIVITY,
    CREATIVE,
    TECHNICAL,
}
