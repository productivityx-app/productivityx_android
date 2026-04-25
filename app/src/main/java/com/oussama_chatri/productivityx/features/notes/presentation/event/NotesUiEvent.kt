package com.oussama_chatri.productivityx.features.notes.presentation.event

sealed class NotesUiEvent {
    data class FilterByTag(val tagId: String?) : NotesUiEvent()
    data object TogglePinnedFilter             : NotesUiEvent()
    data class PinNote(val noteId: String)     : NotesUiEvent()
    data class UnpinNote(val noteId: String)   : NotesUiEvent()
    data class DeleteNote(val noteId: String)  : NotesUiEvent()
    data object Refresh                        : NotesUiEvent()
    data object ClearError                     : NotesUiEvent()
}
