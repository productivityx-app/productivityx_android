package com.oussama_chatri.productivityx.features.notes.presentation.event

sealed class TrashUiEvent {
    data class Restore(val noteId: String)    : TrashUiEvent()
    data class HardDelete(val noteId: String) : TrashUiEvent()
    data object EmptyTrash                    : TrashUiEvent()
}
