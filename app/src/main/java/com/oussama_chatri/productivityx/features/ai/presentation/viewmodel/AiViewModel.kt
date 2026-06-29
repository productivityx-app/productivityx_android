package com.oussama_chatri.productivityx.features.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock
import com.oussama_chatri.productivityx.features.ai.domain.repository.StreamChunk
import com.oussama_chatri.productivityx.features.ai.domain.usecase.CreateConversationUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.ObserveMessagesUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.SendMessageUseCase
import com.oussama_chatri.productivityx.features.ai.presentation.event.AiUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.state.AiPersonaType
import com.oussama_chatri.productivityx.features.ai.presentation.state.AiUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val observeMessages    : ObserveMessagesUseCase,
    private val sendMessage        : SendMessageUseCase,
    private val createConversation : CreateConversationUseCase,
) : ViewModel() {

    private val _state  = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events          = _events.receiveAsFlow()

    private var messagesJob   : Job? = null
    private var streamJob     : Job? = null
    private var createConvJob : Job? = null

    fun onEvent(event: AiUiEvent) {
        when (event) {
            is AiUiEvent.InputChanged     -> _state.update { it.copy(inputText = event.text) }
            is AiUiEvent.SendMessage      -> handleSend()
            is AiUiEvent.SendSuggestion   -> handleSuggestion(event.text)
            is AiUiEvent.NewConversation  -> startNewConversation()
            is AiUiEvent.OpenConversation -> loadConversation(event.id)
            is AiUiEvent.DismissError     -> _state.update { it.copy(error = null) }
            is AiUiEvent.RefreshContext   -> {}
            is AiUiEvent.ConfirmAction    -> executeAction(event.action)
            is AiUiEvent.DismissAction    -> _state.update { it.copy(pendingAction = null) }
            is AiUiEvent.CopyMessage      -> handleCopy(event.messageId)
            is AiUiEvent.RegenerateResponse -> handleRegenerate(event.messageId)
            is AiUiEvent.AddReaction      -> handleReaction(event.messageId, event.emoji)
            is AiUiEvent.ReplyToMessage   -> _state.update { it.copy(replyToMessage = event.message) }
            is AiUiEvent.CancelReply      -> _state.update { it.copy(replyToMessage = null) }
            is AiUiEvent.ToggleEmojiReaction -> {}
            is AiUiEvent.HideEmojiReaction -> _state.update { it.copy(showEmojiReaction = null) }
            is AiUiEvent.ToggleContextPanel -> _state.update { it.copy(isContextExpanded = !it.isContextExpanded) }
            is AiUiEvent.SelectPersona    -> _state.update { it.copy(personaType = event.type) }
        }
    }

    private fun handleCopy(messageId: UUID) {
        _state.value.messages.find { it.id == messageId }?.let { msg ->
            viewModelScope.launch {
                _events.send(UiEvent.ShowSnackbar("Message copied to clipboard"))
            }
        }
    }

    private fun handleRegenerate(messageId: UUID) {
        val msg = _state.value.messages.find { it.id == messageId } ?: return
        val conversationId = _state.value.conversationId ?: return
        doSendMessage(conversationId, msg.content)
    }

    private fun handleReaction(messageId: UUID, emoji: String) {
        viewModelScope.launch {
            _events.send(UiEvent.ShowSnackbar("Reaction added: $emoji"))
        }
    }

    private fun startNewConversation() {
        viewModelScope.launch {
            runCatching { createConversation() }
                .onSuccess { conv -> loadConversation(conv.id) }
                .onFailure { e   -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun loadConversation(conversationId: UUID) {
        messagesJob?.cancel()
        _state.update { it.copy(conversationId = conversationId, messages = emptyList()) }
        messagesJob = viewModelScope.launch {
            observeMessages(conversationId).collect { msgs ->
                _state.update { it.copy(messages = msgs) }
            }
        }
    }

    private fun handleSuggestion(text: String) {
        _state.update { it.copy(inputText = text) }
        handleSend()
    }

    private fun handleSend() {
        val currentState = _state.value
        val content      = currentState.inputText.trim()

        if (content.isBlank()) return
        if (currentState.isStreaming) return
        if (createConvJob?.isActive == true) return

        val conversationId = currentState.conversationId
        if (conversationId == null) {
            createConvJob = viewModelScope.launch {
                runCatching { createConversation(content.take(50)) }
                    .onSuccess { conv ->
                        loadConversation(conv.id)
                        doSendMessage(conv.id, content)
                    }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            return
        }

        doSendMessage(conversationId, content)
    }

    private fun doSendMessage(conversationId: UUID, content: String) {
        _state.update { it.copy(inputText = "", isStreaming = true, streamingContent = "", replyToMessage = null) }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            sendMessage(conversationId, content)
                .catch { e -> _state.update { it.copy(isStreaming = false, error = e.message) } }
                .collect { chunk ->
                    when (chunk) {
                        is StreamChunk.Token -> _state.update {
                            it.copy(streamingContent = it.streamingContent + chunk.text)
                        }
                        is StreamChunk.Done -> _state.update {
                            it.copy(
                                isStreaming      = false,
                                streamingContent = "",
                                pendingAction    = chunk.message.actionBlock,
                            )
                        }
                        is StreamChunk.Error -> _state.update {
                            it.copy(isStreaming = false, error = chunk.cause.message)
                        }
                    }
                }
        }
    }

    private fun executeAction(action: AiActionBlock) {
        viewModelScope.launch {
            _events.send(UiEvent.ShowSnackbar("Action queued \u2014 check your workspace"))
            _state.update { it.copy(pendingAction = null) }
        }
    }
}
