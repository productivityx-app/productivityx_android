package com.oussama_chatri.productivityx.features.ai.presentation.state

import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation

data class ConversationListUiState(
    val conversations : List<Conversation> = emptyList(),
    val isLoading     : Boolean             = false,
    val error         : String?            = null,
    val searchQuery   : String             = "",
    val isSearchVisible: Boolean            = false,
    val pinnedConversations: List<Conversation> = emptyList(),
    val todayConversations: List<Conversation> = emptyList(),
    val yesterdayConversations: List<Conversation> = emptyList(),
    val earlierConversations: List<Conversation> = emptyList(),
    val fabExpanded  : Boolean             = false,
)
