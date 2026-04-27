package com.oussama_chatri.productivityx.features.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.ai.domain.usecase.DeleteConversationUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.ObserveConversationsUseCase
import com.oussama_chatri.productivityx.features.ai.presentation.event.ConversationListUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.state.ConversationListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val observeConversations : ObserveConversationsUseCase,
    private val deleteConversation   : DeleteConversationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListUiState(isLoading = true))
    val state: StateFlow<ConversationListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeConversations()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _state.update { it.copy(conversations = list, isLoading = false) }
                }
        }
    }

    fun onEvent(event: ConversationListUiEvent) {
        when (event) {
            is ConversationListUiEvent.DeleteConversation -> deleteById(event.id)
            else                                          -> Unit
        }
    }

    private fun deleteById(id: UUID) {
        viewModelScope.launch {
            runCatching { deleteConversation(id) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }
}
