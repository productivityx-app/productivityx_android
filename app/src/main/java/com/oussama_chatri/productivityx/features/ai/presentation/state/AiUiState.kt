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
    // Active action card waiting for user confirmation
    val pendingAction   : com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock? = null,
)
