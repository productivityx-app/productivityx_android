package com.oussama_chatri.productivityx.features.ai.presentation.event

import java.util.UUID

sealed class ConversationListUiEvent {
    data class OpenConversation(val id: UUID) : ConversationListUiEvent()
    data class DeleteConversation(val id: UUID) : ConversationListUiEvent()
    data object CreateConversation : ConversationListUiEvent()
}
