package com.oussama_chatri.productivityx.features.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.usecase.DeleteConversationUseCase
import com.oussama_chatri.productivityx.features.ai.domain.usecase.ObserveConversationsUseCase
import com.oussama_chatri.productivityx.features.ai.presentation.event.ConversationListUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.state.ConversationListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val observeConversations : ObserveConversationsUseCase,
    private val deleteConversation   : DeleteConversationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListUiState(isLoading = true))
    val state: StateFlow<ConversationListUiState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var allConversations: List<Conversation> = emptyList()

    init {
        observeJob = viewModelScope.launch {
            observeConversations()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    allConversations = list
                    updateGroupedState()
                }
        }
    }

    fun onEvent(event: ConversationListUiEvent) {
        when (event) {
            is ConversationListUiEvent.OpenConversation      -> {}
            is ConversationListUiEvent.DeleteConversation    -> deleteById(event.id)
            is ConversationListUiEvent.ArchiveConversation   -> archiveById(event.id)
            is ConversationListUiEvent.PinConversation       -> pinById(event.id)
            is ConversationListUiEvent.UnpinConversation     -> unpinById(event.id)
            is ConversationListUiEvent.SearchQueryChanged    -> {
                _state.update { it.copy(searchQuery = event.query) }
                filterConversations(event.query)
            }
            is ConversationListUiEvent.ToggleSearch          -> {
                _state.update { it.copy(isSearchVisible = !it.isSearchVisible, searchQuery = "") }
                updateGroupedState()
            }
            is ConversationListUiEvent.CreateConversation    -> {}
            is ConversationListUiEvent.ToggleFab             -> {
                _state.update { it.copy(fabExpanded = !it.fabExpanded) }
            }
        }
    }

    private fun updateGroupedState() {
        val query = _state.value.searchQuery
        val list = if (query.isNotBlank()) {
            allConversations.filter {
                it.title?.contains(query, ignoreCase = true) == true ||
                it.lastMessage?.contains(query, ignoreCase = true) == true
            }
        } else {
            allConversations
        }
        groupConversations(list)
    }

    private fun groupConversations(list: List<Conversation>) {
        val pinned = list.filter { it.isPinned }
        val unpinned = list.filter { !it.isPinned }

        val now = LocalDate.now(ZoneId.systemDefault())
        val today = mutableListOf<Conversation>()
        val yesterday = mutableListOf<Conversation>()
        val earlier = mutableListOf<Conversation>()

        unpinned.forEach { conv ->
            val convDate = conv.updatedAt.atZone(ZoneId.systemDefault()).toLocalDate()
            when {
                convDate == now -> today.add(conv)
                convDate == now.minusDays(1) -> yesterday.add(conv)
                else -> earlier.add(conv)
            }
        }

        _state.update {
            it.copy(
                conversations = list,
                pinnedConversations = pinned,
                todayConversations = today,
                yesterdayConversations = yesterday,
                earlierConversations = earlier,
                isLoading = false,
            )
        }
    }

    private fun filterConversations(query: String) {
        if (query.isBlank()) {
            updateGroupedState()
            return
        }
        val filtered = allConversations.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
            it.lastMessage?.contains(query, ignoreCase = true) == true
        }
        _state.update { it.copy(conversations = filtered, isLoading = false) }
    }

    private fun deleteById(id: UUID) {
        viewModelScope.launch {
            runCatching { deleteConversation(id) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    private fun archiveById(id: UUID) {
        allConversations.find { it.id == id }?.let { conv ->
            viewModelScope.launch {
                runCatching { /* archive logic */ }
                updateGroupedState()
            }
        }
    }

    private fun pinById(id: UUID) {
        viewModelScope.launch {
            runCatching { /* pin logic - DAO pin(id) */ }
            updateGroupedState()
        }
    }

    private fun unpinById(id: UUID) {
        viewModelScope.launch {
            runCatching { /* unpin logic - DAO unpin(id) */ }
            updateGroupedState()
        }
    }
}
