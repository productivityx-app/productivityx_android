package com.oussama_chatri.productivityx.features.notes.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun observeActiveNotes(tagId: String? = null, pinnedOnly: Boolean = false): Flow<List<Note>>

    fun observeTrash(): Flow<List<Note>>

    suspend fun getNoteById(noteId: String): Resource<Note>

    suspend fun createNote(
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?
    ): Resource<Note>

    suspend fun updateNote(
        noteId: String,
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?
    ): Resource<Note>

    suspend fun pinNote(noteId: String): Resource<Note>

    suspend fun unpinNote(noteId: String): Resource<Note>

    suspend fun softDeleteNote(noteId: String): Resource<Note>

    suspend fun restoreNote(noteId: String): Resource<Note>

    suspend fun hardDeleteNote(noteId: String): Resource<Unit>

    suspend fun addTagToNote(noteId: String, tagId: String): Resource<Note>

    suspend fun removeTagFromNote(noteId: String, tagId: String): Resource<Note>

    suspend fun refreshNotes(): Resource<Unit>
}

interface TagRepository {

    fun observeTags(): Flow<List<Tag>>

    suspend fun getTags(): Resource<List<Tag>>

    suspend fun createTag(name: String, color: String?): Resource<Tag>

    suspend fun updateTag(tagId: String, name: String, color: String?): Resource<Tag>

    suspend fun deleteTag(tagId: String): Resource<Unit>

    suspend fun refreshTags(): Resource<Unit>
}
