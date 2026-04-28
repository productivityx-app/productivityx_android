package com.oussama_chatri.productivityx.features.ai.presentation.event

sealed class AiUiEvent {
    data class InputChanged(val text: String)               : AiUiEvent()
    data object SendMessage                                 : AiUiEvent()
    data class SendSuggestion(val text: String)             : AiUiEvent()
    data object NewConversation                             : AiUiEvent()
    data class OpenConversation(val id: java.util.UUID)     : AiUiEvent()
    data object DismissError                                : AiUiEvent()
    data object RefreshContext                              : AiUiEvent()
    data class ConfirmAction(
        val action: com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock
    )                                                       : AiUiEvent()
    data object DismissAction                               : AiUiEvent()
}