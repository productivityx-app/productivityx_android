package com.oussama_chatri.productivityx.features.notes.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.domain.usecase.CreateNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.CreateTagUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.GetNoteByIdUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveTagsUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.PinNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.SoftDeleteNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.UnpinNoteUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.UpdateNoteUseCase
import com.oussama_chatri.productivityx.features.notes.presentation.event.NoteEditorUiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.state.NoteEditorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
class NoteEditorViewModel @Inject constructor(
    private val getNoteById: GetNoteByIdUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val pinNote: PinNoteUseCase,
    private val unpinNote: UnpinNoteUseCase,
    private val softDeleteNote: SoftDeleteNoteUseCase,
    private val observeTags: ObserveTagsUseCase,
    private val createTag: CreateTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        observeTags().onEach { tags ->
            _allTags.update { tags }
        }.catch {}.launchIn(viewModelScope)
    }

    fun init(noteId: String?) {
        if (noteId == null) return
        if (_uiState.value.noteId == noteId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = getNoteById(noteId)) {
                is Resource.Success -> {
                    val note = result.data
                    _uiState.update {
                        it.copy(
                            noteId            = note.id,
                            title             = note.title,
                            content           = note.content,
                            tags              = note.tags,
                            isPinned          = note.isPinned,
                            isDeleted         = note.isDeleted,
                            hasUnsavedChanges = false,
                            isSaving          = false
                        )
                    }
                }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isSaving = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun onEvent(event: NoteEditorUiEvent) {
        when (event) {
            is NoteEditorUiEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value, hasUnsavedChanges = true) }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.ContentChanged -> {
                _uiState.update { it.copy(content = event.value, hasUnsavedChanges = true) }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.AddTag -> {
                val tag = _allTags.value.firstOrNull { it.id == event.tagId } ?: return
                _uiState.update { it.copy(tags = it.tags + tag, hasUnsavedChanges = true) }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.RemoveTag -> {
                _uiState.update {
                    it.copy(
                        tags              = it.tags.filterNot { t -> t.id == event.tagId }.toSet(),
                        hasUnsavedChanges = true
                    )
                }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.CreateTag -> handleCreateTag(event.name, event.color)
            NoteEditorUiEvent.TogglePin    -> togglePin()
            NoteEditorUiEvent.Save         -> saveNow()
            NoteEditorUiEvent.DeleteNote   -> deleteNote()
            NoteEditorUiEvent.ClearError   -> _uiState.update { it.copy(error = null) }
        }
    }

    // Creates the tag remotely/locally, then immediately adds it to the current note
    private fun handleCreateTag(name: String, color: String) {
        viewModelScope.launch {
            when (val result = createTag(name, color)) {
                is Resource.Success -> {
                    val newTag = result.data
                    // The tag now appears in allTags via ObserveTagsUseCase automatically.
                    // Also add it to this note immediately without waiting for the next recomposition.
                    _uiState.update {
                        it.copy(
                            tags              = it.tags + newTag,
                            hasUnsavedChanges = true
                        )
                    }
                    scheduleAutoSave()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500)
            saveNow()
        }
    }

    private fun saveNow() {
        val state = _uiState.value
        if (!state.hasUnsavedChanges && state.noteId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val tagIds = state.tags.map { it.id }.toSet()
            val result = if (state.noteId == null) {
                createNote(
                    title   = state.title.trim().ifBlank { null },
                    content = state.content.ifBlank { null },
                    tagIds  = tagIds.ifEmpty { null },
                    pinned  = if (state.isPinned) true else null
                )
            } else {
                updateNote(
                    noteId  = state.noteId,
                    title   = state.title.trim(),
                    content = state.content,
                    tagIds  = tagIds,
                    pinned  = state.isPinned
                )
            }
            when (result) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        noteId            = result.data.id,
                        hasUnsavedChanges = false,
                        isSaving          = false
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.message, isSaving = false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun togglePin() {
        val state  = _uiState.value
        val noteId = state.noteId ?: return
        viewModelScope.launch {
            if (state.isPinned) unpinNote(noteId) else pinNote(noteId)
            _uiState.update { it.copy(isPinned = !it.isPinned) }
        }
    }

    private fun deleteNote() {
        val noteId = _uiState.value.noteId ?: return
        viewModelScope.launch {
            softDeleteNote(noteId)
            _events.send(UiEvent.NavigateBack)
        }
    }
}