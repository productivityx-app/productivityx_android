package com.oussama_chatri.productivityx.features.notes.presentation.state

import com.oussama_chatri.productivityx.features.notes.domain.model.Tag

data class NoteEditorUiState(
    val noteId: String?            = null,
    val title: String              = "",
    val content: String            = "",
    val tags: Set<Tag>             = emptySet(),
    val isPinned: Boolean          = false,
    val isDeleted: Boolean         = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean          = false,
    val error: String?             = null
)
