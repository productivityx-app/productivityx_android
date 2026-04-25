package com.oussama_chatri.productivityx.features.notes.presentation.state

import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag

data class NotesUiState(
    val notes: List<Note>       = emptyList(),
    val tags: List<Tag>         = emptyList(),
    val selectedTagId: String?  = null,
    val showPinnedOnly: Boolean = false,
    val isLoading: Boolean      = false,
    val isRefreshing: Boolean   = false,
    val error: String?          = null
)
