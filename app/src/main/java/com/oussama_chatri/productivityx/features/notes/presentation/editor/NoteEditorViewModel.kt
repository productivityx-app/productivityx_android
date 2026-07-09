package com.oussama_chatri.productivityx.features.notes.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.SyncStatus
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
import com.oussama_chatri.productivityx.features.notes.presentation.state.EditorFocusMode
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
import java.time.Instant
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
    private var metadataJob: Job? = null

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
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            plainTextContent = note.plainTextContent,
                            tags = note.tags,
                            imageUrls = note.imageUrls,
                            isPinned = note.isPinned,
                            isDeleted = note.isDeleted,
                            hasUnsavedChanges = false,
                            isSaving = false,
                            lastSavedAt = note.updatedAt,
                            wordCount = note.wordCount,
                            characterCount = note.plainTextContent.length,
                            readingTimeSeconds = note.readingTimeSeconds,
                            syncStatus = note.syncStatus
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message, isSaving = false) }
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
                updateContent(event.value)
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
                        tags = it.tags.filterNot { t -> t.id == event.tagId }.toSet(),
                        hasUnsavedChanges = true
                    )
                }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.AddImage -> {
                _uiState.update { it.copy(imageUrls = it.imageUrls + event.uri, hasUnsavedChanges = true) }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.RemoveImage -> {
                _uiState.update {
                    it.copy(
                        imageUrls = it.imageUrls.filterNot { uri -> uri == event.uri },
                        hasUnsavedChanges = true
                    )
                }
                scheduleAutoSave()
            }
            is NoteEditorUiEvent.CreateTag -> handleCreateTag(event.name, event.color)
            NoteEditorUiEvent.TogglePin -> togglePin()
            NoteEditorUiEvent.Save -> saveNow()
            NoteEditorUiEvent.DeleteNote -> deleteNote()
            NoteEditorUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is NoteEditorUiEvent.SetFocusMode -> _uiState.update { it.copy(focusMode = event.mode) }
            NoteEditorUiEvent.ToggleFocusMode -> {
                val next = when (_uiState.value.focusMode) {
                    EditorFocusMode.NORMAL -> EditorFocusMode.FOCUS
                    EditorFocusMode.FOCUS -> EditorFocusMode.TYPEWRITER
                    EditorFocusMode.TYPEWRITER -> EditorFocusMode.NORMAL
                }
                _uiState.update { it.copy(focusMode = next) }
            }
            NoteEditorUiEvent.ToggleMetadata -> _uiState.update { it.copy(showMetadata = !it.showMetadata) }
            NoteEditorUiEvent.ShowExportSheet -> _uiState.update { it.copy(showExportSheet = true) }
            NoteEditorUiEvent.HideExportSheet -> _uiState.update { it.copy(showExportSheet = false) }
            NoteEditorUiEvent.TogglePreviewMode -> _uiState.update { it.copy(isPreviewMode = !it.isPreviewMode) }
            NoteEditorUiEvent.RequestFocus -> {}
        }
    }

    private fun updateContent(value: String) {
        val plain = stripMarkdown(value)
        val words = wordCount(plain)
        val readingSecs = readingTimeSeconds(words)
        _uiState.update {
            it.copy(
                content = value,
                plainTextContent = plain,
                wordCount = words,
                characterCount = value.length,
                readingTimeSeconds = readingSecs,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleCreateTag(name: String, color: String) {
        viewModelScope.launch {
            when (val result = createTag(name, color)) {
                is Resource.Success -> {
                    val newTag = result.data
                    _uiState.update {
                        it.copy(
                            tags = it.tags + newTag,
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
            _uiState.update { it.copy(isSaving = true, syncStatus = SyncStatus.PENDING) }
            val tagIds = state.tags.map { it.id }.toSet()
            val result = if (state.noteId == null) {
                createNote(
                    title = state.title.trim().ifBlank { null },
                    content = state.content.ifBlank { null },
                    tagIds = tagIds.ifEmpty { null },
                    pinned = if (state.isPinned) true else null,
                    imageUrls = state.imageUrls.ifEmpty { null }
                )
            } else {
                updateNote(
                    noteId = state.noteId,
                    title = state.title.trim(),
                    content = state.content,
                    tagIds = tagIds,
                    pinned = state.isPinned,
                    imageUrls = state.imageUrls
                )
            }
            when (result) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        noteId = result.data.id,
                        hasUnsavedChanges = false,
                        isSaving = false,
                        lastSavedAt = Instant.now(),
                        syncStatus = SyncStatus.SYNCED
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.message, isSaving = false, syncStatus = SyncStatus.CONFLICT)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun togglePin() {
        val state = _uiState.value
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

    private fun stripMarkdown(md: String): String {
        val pattern = Regex(
            "(!?\\[([^\\]]*)\\]\\([^)]*\\))" +
                    "|(```[\\s\\S]*?```)" +
                    "|(`.+?`)" +
                    "|(^#{1,6}\\s)" +
                    "|(\\*{1,3}|_{1,3})" +
                    "|(~~.+?~~)" +
                    "|(^[-*+]\\s|^\\d+\\.\\s)" +
                    "|(^>+\\s?)" +
                    "|(^---+$|^===+$)" +
                    "|(\\n{2,})"
        )
        return Regex("\\s{2,}").replace(pattern.replace(md, " "), " ").trim()
    }

    private fun wordCount(plain: String): Int =
        if (plain.isBlank()) 0 else plain.trim().split(Regex("\\s+")).size

    private fun readingTimeSeconds(words: Int): Int =
        if (words == 0) 0 else (words.toDouble() / 200.0 * 60).toInt().coerceAtLeast(1)
}
