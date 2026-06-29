package com.oussama_chatri.productivityx.features.notes.presentation.state

import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag

enum class NoteViewMode { GRID, LIST, COMPACT }

enum class NoteSortMode { DATE, TITLE, MANUAL }

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val folders: List<NoteFolder> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
    val selectedTagId: String? = null,
    val showPinnedOnly: Boolean = false,
    val viewMode: NoteViewMode = NoteViewMode.GRID,
    val sortMode: NoteSortMode = NoteSortMode.DATE,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: List<Note> = emptyList(),
    val selectedNoteIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedFolderId: String? = null
)
