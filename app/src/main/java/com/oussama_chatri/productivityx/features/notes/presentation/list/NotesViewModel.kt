package com.oussama_chatri.productivityx.features.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.usecase.AddTagToNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.GetPagedNotesUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveActiveNotesUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveFoldersUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveSearchUseCase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeActiveNotes: ObserveActiveNotesUseCase,
    private val getPagedNotes: GetPagedNotesUseCase,
    private val observeTags: ObserveTagsUseCase,
    private val observeFolders: ObserveFoldersUseCase,
    private val observeSearch: ObserveSearchUseCase,
    private val pinNote: PinNoteUseCase,
    private val unpinNote: UnpinNoteUseCase,
    private val softDeleteNote: SoftDeleteNoteUseCase,
    private val addTagToNote: AddTagToNoteUseCase,
    private val refreshNotes: RefreshNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _pagedNotes = MutableStateFlow<PagingData<Note>>(PagingData.empty())
    val pagedNotes: StateFlow<PagingData<Note>> = _pagedNotes.asStateFlow()

    private var notesJob: Job? = null
    private var searchJob: Job? = null

    init {
        observeTags().onEach { tags ->
            _uiState.update { it.copy(tags = tags) }
        }.catch {}.launchIn(viewModelScope)

        observeFolders().onEach { folders ->
            _uiState.update { it.copy(folders = folders) }
        }.catch {}.launchIn(viewModelScope)

        observeNotesList()
    }

    fun onEvent(event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.FilterByTag -> {
                _uiState.update { it.copy(selectedTagId = event.tagId, selectedTagIds = emptySet(), showPinnedOnly = false) }
                observeNotesList()
            }
            is NotesUiEvent.FilterByTags -> {
                _uiState.update { it.copy(selectedTagIds = event.tagIds, selectedTagId = null, showPinnedOnly = false) }
                observeNotesList()
            }
            is NotesUiEvent.FilterByFolder -> {
                _uiState.update { it.copy(selectedFolderId = event.folderId) }
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
                _events.send(UiEvent.ShowSnackbar("Note moved to trash"))
            }
            NotesUiEvent.Refresh -> refresh()
            NotesUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is NotesUiEvent.SetViewMode -> _uiState.update { it.copy(viewMode = event.mode) }
            is NotesUiEvent.SetSortMode -> _uiState.update { it.copy(sortMode = event.mode) }
            is NotesUiEvent.SetSearchQuery -> handleSearch(event.query)
            NotesUiEvent.ToggleSearch -> {
                _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
                if (!_uiState.value.isSearchActive) {
                    searchJob?.cancel()
                    observeNotesList()
                }
            }
            NotesUiEvent.ClearSearch -> {
                _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearchActive = false) }
                searchJob?.cancel()
                observeNotesList()
            }
            is NotesUiEvent.ToggleNoteSelection -> toggleSelection(event.noteId)
            NotesUiEvent.ClearSelection -> clearSelection()
            NotesUiEvent.BulkDelete -> bulkDelete()
            NotesUiEvent.BulkArchive -> bulkArchive()
            is NotesUiEvent.BulkAddTag -> bulkAddTag(event.tagId)
            NotesUiEvent.BulkPin -> bulkPin()
            is NotesUiEvent.SwipePin -> viewModelScope.launch {
                val note = _uiState.value.notes.find { it.id == event.noteId }
                if (note != null) {
                    if (note.isPinned) unpinNote(note.id) else pinNote(note.id)
                }
            }
            is NotesUiEvent.SwipeArchive -> viewModelScope.launch {
                softDeleteNote(event.noteId)
                _events.send(UiEvent.ShowSnackbar("Note archived"))
            }
        }
    }

    private fun handleSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = observeSearch(query)
            .debounce(300)
            .distinctUntilChanged()
            .onEach { results ->
                _uiState.update { it.copy(searchResults = results) }
            }.catch {}.launchIn(viewModelScope)
    }

    private fun toggleSelection(noteId: String) {
        _uiState.update { state ->
            val newSelection = if (noteId in state.selectedNoteIds) {
                state.selectedNoteIds - noteId
            } else {
                state.selectedNoteIds + noteId
            }
            state.copy(
                selectedNoteIds = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun clearSelection() {
        _uiState.update { it.copy(selectedNoteIds = emptySet(), isSelectionMode = false) }
    }

    private fun bulkDelete() {
        viewModelScope.launch {
            _uiState.value.selectedNoteIds.forEach { id -> softDeleteNote(id) }
            _events.send(UiEvent.ShowSnackbar("${_uiState.value.selectedNoteIds.size} notes moved to trash"))
            clearSelection()
        }
    }

    private fun bulkArchive() {
        viewModelScope.launch {
            _uiState.value.selectedNoteIds.forEach { id -> softDeleteNote(id) }
            _events.send(UiEvent.ShowSnackbar("${_uiState.value.selectedNoteIds.size} notes archived"))
            clearSelection()
        }
    }

    private fun bulkAddTag(tagId: String) {
        viewModelScope.launch {
            _uiState.value.selectedNoteIds.forEach { id -> addTagToNote(id, tagId) }
            _events.send(UiEvent.ShowSnackbar("Tag added to ${_uiState.value.selectedNoteIds.size} notes"))
        }
    }

    private fun bulkPin() {
        viewModelScope.launch {
            _uiState.value.selectedNoteIds.forEach { id -> pinNote(id) }
            _events.send(UiEvent.ShowSnackbar("${_uiState.value.selectedNoteIds.size} notes pinned"))
            clearSelection()
        }
    }

    private fun observeNotesList() {
        notesJob?.cancel()
        val state = _uiState.value

        // Use Paging for the main list
        getPagedNotes(
            tagId = state.selectedTagId,
            pinnedOnly = state.showPinnedOnly,
            tagIds = state.selectedTagIds.toList().ifEmpty { null },
            folderId = state.selectedFolderId
        ).cachedIn(viewModelScope)
            .onEach { pagingData ->
                _pagedNotes.value = pagingData
            }.launchIn(viewModelScope)

        // Keep observing the non-paged list for selection and bulk actions
        // (Optional: depending on how many notes we expect)
        notesJob = observeActiveNotes(
            tagId = state.selectedTagId,
            pinnedOnly = state.showPinnedOnly,
            tagIds = state.selectedTagIds.toList().ifEmpty { null },
            folderId = state.selectedFolderId
        ).onEach { notes ->
            val sorted = when (state.sortMode) {
                com.oussama_chatri.productivityx.features.notes.presentation.state.NoteSortMode.TITLE -> notes.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                com.oussama_chatri.productivityx.features.notes.presentation.state.NoteSortMode.MANUAL -> notes
                com.oussama_chatri.productivityx.features.notes.presentation.state.NoteSortMode.DATE -> notes
            }
            _uiState.update { it.copy(notes = sorted, isLoading = false) }
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
