package com.oussama_chatri.productivityx.features.notes.presentation.event

sealed class NoteEditorUiEvent {
    data class TitleChanged(val value: String)              : NoteEditorUiEvent()
    data class ContentChanged(val value: String)            : NoteEditorUiEvent()
    data class AddTag(val tagId: String)                    : NoteEditorUiEvent()
    data class RemoveTag(val tagId: String)                 : NoteEditorUiEvent()
    data class CreateTag(val name: String, val color: String) : NoteEditorUiEvent()
    data object TogglePin                                   : NoteEditorUiEvent()
    data object Save                                        : NoteEditorUiEvent()
    data object DeleteNote                                  : NoteEditorUiEvent()
    data object ClearError                                  : NoteEditorUiEvent()
}