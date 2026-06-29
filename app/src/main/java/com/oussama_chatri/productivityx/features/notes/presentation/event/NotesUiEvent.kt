package com.oussama_chatri.productivityx.features.notes.presentation.event

import com.oussama_chatri.productivityx.features.notes.presentation.state.NoteSortMode
import com.oussama_chatri.productivityx.features.notes.presentation.state.NoteViewMode

sealed class NotesUiEvent {
    data class FilterByTag(val tagId: String?) : NotesUiEvent()
    data class FilterByTags(val tagIds: Set<String>) : NotesUiEvent()
    data class FilterByFolder(val folderId: String?) : NotesUiEvent()
    data object TogglePinnedFilter : NotesUiEvent()
    data class PinNote(val noteId: String) : NotesUiEvent()
    data class UnpinNote(val noteId: String) : NotesUiEvent()
    data class DeleteNote(val noteId: String) : NotesUiEvent()
    data object Refresh : NotesUiEvent()
    data object ClearError : NotesUiEvent()
    data class SetViewMode(val mode: NoteViewMode) : NotesUiEvent()
    data class SetSortMode(val mode: NoteSortMode) : NotesUiEvent()
    data class SetSearchQuery(val query: String) : NotesUiEvent()
    data object ToggleSearch : NotesUiEvent()
    data object ClearSearch : NotesUiEvent()
    data class ToggleNoteSelection(val noteId: String) : NotesUiEvent()
    data object ClearSelection : NotesUiEvent()
    data object BulkDelete : NotesUiEvent()
    data object BulkArchive : NotesUiEvent()
    data class BulkAddTag(val tagId: String) : NotesUiEvent()
    data object BulkPin : NotesUiEvent()
    data class SwipePin(val noteId: String) : NotesUiEvent()
    data class SwipeArchive(val noteId: String) : NotesUiEvent()
}
