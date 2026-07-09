package com.oussama_chatri.productivityx.features.ai.presentation.event

import com.oussama_chatri.productivityx.features.ai.domain.model.Message

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
    data object StopGenerating                               : AiUiEvent()
    data class CopyMessage(val messageId: java.util.UUID)   : AiUiEvent()
    data class RegenerateResponse(val messageId: java.util.UUID) : AiUiEvent()
    data class AddReaction(val messageId: java.util.UUID, val emoji: String) : AiUiEvent()
    data class ReplyToMessage(val message: Message)         : AiUiEvent()
    data object CancelReply                                 : AiUiEvent()
    data object ToggleEmojiReaction                         : AiUiEvent()
    data object HideEmojiReaction                           : AiUiEvent()
    data object ToggleContextPanel                          : AiUiEvent()
    data class SelectPersona(val type: com.oussama_chatri.productivityx.features.ai.presentation.state.AiPersonaType) : AiUiEvent()
}