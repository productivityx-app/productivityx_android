package com.oussama_chatri.productivityx.features.notes.presentation.state

import com.oussama_chatri.productivityx.features.notes.domain.model.Note

data class TrashUiState(
    val notes: List<Note>  = emptyList(),
    val isLoading: Boolean = false,
    val error: String?     = null
)
