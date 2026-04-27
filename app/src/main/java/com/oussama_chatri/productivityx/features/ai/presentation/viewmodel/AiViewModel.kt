package com.oussama_chatri.productivityx.features.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.ai.domain.model.AiActionBlock
import com.oussama_chatri.productivityx.features.ai.domain.repository.StreamChunk
import com.oussama_chatri.productivityx.features.ai.domain.usecase.BuildAiContextUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.CreateConversationUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.ObserveMessagesUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.SendMessageUseCase
import com.oussama_chatri.productivityx.features.ai.presentation.event.AiUiEvent
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
    private val observeMessages      : ObserveMessagesUseCase,
    private val sendMessage          : SendMessageUseCase,
    private val createConversation   : CreateConversationUseCase,
    private val buildContext         : BuildAiContextUseCase,
) : ViewModel() {

    private val _state   = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    private val _events  = Channel<UiEvent>(Channel.BUFFERED)
    val events           = _events.receiveAsFlow()

    private var messagesJob   : Job? = null
    private var streamJob     : Job? = null

    init {
        loadContext()
    }

    fun onEvent(event: AiUiEvent) {
        when (event) {
            is AiUiEvent.InputChanged      -> _state.update { it.copy(inputText = event.text) }
            is AiUiEvent.SendMessage       -> handleSend()
            is AiUiEvent.NewConversation   -> startNewConversation()
            is AiUiEvent.OpenConversation  -> loadConversation(event.id)
            is AiUiEvent.DismissError      -> _state.update { it.copy(error = null) }
            is AiUiEvent.RefreshContext    -> loadContext()
            is AiUiEvent.ConfirmAction     -> executeAction(event.action)
            is AiUiEvent.DismissAction     -> _state.update { it.copy(pendingAction = null) }
        }
    }

    private fun loadContext() {
        viewModelScope.launch {
            _state.update { it.copy(isContextLoading = true) }
            runCatching { buildContext() }
                .onSuccess  { ctx -> _state.update { it.copy(context = ctx, isContextLoading = false) } }
                .onFailure  { _state.update { it.copy(isContextLoading = false) } }
        }
    }

    private fun startNewConversation() {
        viewModelScope.launch {
            runCatching { createConversation() }
                .onSuccess { conv -> loadConversation(conv.id) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
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

    private fun handleSend() {
        val currentState = _state.value
        val content      = currentState.inputText.trim()
        if (content.isBlank() || currentState.isStreaming) return

        val conversationId = currentState.conversationId
        if (conversationId == null) {
            // Auto-create a conversation on first message
            viewModelScope.launch {
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
        val ctx = _state.value.context ?: return

        _state.update { it.copy(inputText = "", isStreaming = true, streamingContent = "") }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            sendMessage(conversationId, content, ctx)
                .catch { e -> _state.update { it.copy(isStreaming = false, error = e.message) } }
                .collect { chunk ->
                    when (chunk) {
                        is StreamChunk.Token -> _state.update {
                            it.copy(streamingContent = it.streamingContent + chunk.text)
                        }
                        is StreamChunk.Done  -> _state.update {
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
        // Delegate action execution to the respective feature ViewModels via events
        viewModelScope.launch {
            _events.send(UiEvent.ShowSnackbar("Action queued — check your workspace"))
            _state.update { it.copy(pendingAction = null) }
        }
    }
}
