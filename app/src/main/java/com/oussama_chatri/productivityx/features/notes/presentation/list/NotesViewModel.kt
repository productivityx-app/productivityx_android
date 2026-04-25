package com.oussama_chatri.productivityx.features.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveActiveNotesUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveTagsUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.PinNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.RefreshNotesUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.SoftDeleteNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.UnpinNoteUseCase
import com.oussama_chatri.productivityx.features.notes.presentation.event.NotesUiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.state.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeActiveNotes: ObserveActiveNotesUseCase,
    private val observeTags: ObserveTagsUseCase,
    private val pinNote: PinNoteUseCase,
    private val unpinNote: UnpinNoteUseCase,
    private val softDeleteNote: SoftDeleteNoteUseCase,
    private val refreshNotes: RefreshNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var notesJob: Job? = null

    init {
        observeTags().onEach { tags ->
            _uiState.update { it.copy(tags = tags) }
        }.catch {}.launchIn(viewModelScope)

        observeNotesList()
    }

    fun onEvent(event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.FilterByTag -> {
                _uiState.update { it.copy(selectedTagId = event.tagId, showPinnedOnly = false) }
                observeNotesList()
            }
            NotesUiEvent.TogglePinnedFilter -> {
                _uiState.update { it.copy(showPinnedOnly = !it.showPinnedOnly, selectedTagId = null) }
                observeNotesList()
            }
            is NotesUiEvent.PinNote -> viewModelScope.launch {
                pinNote(event.noteId)
            }
            is NotesUiEvent.UnpinNote -> viewModelScope.launch {
                unpinNote(event.noteId)
            }
            is NotesUiEvent.DeleteNote -> viewModelScope.launch {
                softDeleteNote(event.noteId)
                _events.send(UiEvent.ShowSnackbar("Note moved to trash", "Undo"))
            }
            NotesUiEvent.Refresh -> refresh()
            NotesUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun observeNotesList() {
        notesJob?.cancel()
        val state = _uiState.value
        notesJob = observeActiveNotes(
            tagId      = state.selectedTagId,
            pinnedOnly = state.showPinnedOnly
        ).onEach { notes ->
            _uiState.update { it.copy(notes = notes, isLoading = false) }
        }.catch { e ->
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshNotes()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
