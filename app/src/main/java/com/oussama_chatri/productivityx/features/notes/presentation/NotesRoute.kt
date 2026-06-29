package com.oussama_chatri.productivityx.features.notes.presentation

import kotlinx.serialization.Serializable

sealed interface NotesRoute {

    @Serializable
    data object NotesList : NotesRoute

    @Serializable
    data class NoteEditor(val noteId: String? = null) : NotesRoute

    @Serializable
    data object Trash : NotesRoute

    @Serializable
    data object TagManagement : NotesRoute

    @Serializable
    data object FolderManagement : NotesRoute

    @Serializable
    data object TemplateGallery : NotesRoute
}
