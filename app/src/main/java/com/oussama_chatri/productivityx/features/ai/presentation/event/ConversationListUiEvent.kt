package com.oussama_chatri.productivityx.features.ai.presentation.event

import java.util.UUID

sealed class ConversationListUiEvent {
    data class OpenConversation(val id: UUID) : ConversationListUiEvent()
    data class DeleteConversation(val id: UUID) : ConversationListUiEvent()
    data class ArchiveConversation(val id: UUID) : ConversationListUiEvent()
    data class PinConversation(val id: UUID) : ConversationListUiEvent()
    data class UnpinConversation(val id: UUID) : ConversationListUiEvent()
    data class SearchQueryChanged(val query: String) : ConversationListUiEvent()
    data object ToggleSearch : ConversationListUiEvent()
    data object CreateConversation : ConversationListUiEvent()
    data object ToggleFab : ConversationListUiEvent()
}
