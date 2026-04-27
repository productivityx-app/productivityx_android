package com.oussama_chatri.productivityx.features.ai.presentation.state

import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation

data class ConversationListUiState(
    val conversations : List<Conversation> = emptyList(),
    val isLoading     : Boolean             = false,
    val error         : String?            = null,
)
