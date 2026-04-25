package com.oussama_chatri.productivityx.features.notes.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.usecase.HardDeleteNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveTrashUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.RestoreNoteUseCase
import com.oussama_chatri.productivityx.features.notes.presentation.event.TrashUiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.state.TrashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
class TrashViewModel @Inject constructor(
    private val observeTrash: ObserveTrashUseCase,
    private val restoreNote: RestoreNoteUseCase,
    private val hardDeleteNote: HardDeleteNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeTrash().onEach { notes ->
            _uiState.update { it.copy(notes = notes, isLoading = false) }
        }.catch { e ->
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: TrashUiEvent) {
        when (event) {
            is TrashUiEvent.Restore -> viewModelScope.launch {
                restoreNote(event.noteId)
                _events.send(UiEvent.ShowSnackbar("Note restored"))
            }
            is TrashUiEvent.HardDelete -> viewModelScope.launch {
                hardDeleteNote(event.noteId)
            }
            TrashUiEvent.EmptyTrash -> viewModelScope.launch {
                _uiState.value.notes.forEach { hardDeleteNote(it.id) }
                _events.send(UiEvent.ShowSnackbar("Trash emptied"))
            }
        }
    }
}
