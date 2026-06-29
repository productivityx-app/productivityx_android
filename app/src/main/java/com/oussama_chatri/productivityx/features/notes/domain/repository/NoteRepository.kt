package com.oussama_chatri.productivityx.features.notes.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteLink
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun observeActiveNotes(tagId: String? = null, pinnedOnly: Boolean = false, tagIds: List<String>? = null, folderId: String? = null): Flow<List<Note>>

    fun getPagedActiveNotes(tagId: String? = null, pinnedOnly: Boolean = false, tagIds: List<String>? = null, folderId: String? = null): Flow<PagingData<Note>>

    fun observeTrash(): Flow<List<Note>>

    fun observeSearch(query: String): Flow<List<Note>>

    suspend fun getNoteById(noteId: String): Resource<Note>

    suspend fun createNote(
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?,
        folderId: String? = null
    ): Resource<Note>

    suspend fun updateNote(
        noteId: String,
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?,
        folderId: String? = null
    ): Resource<Note>

    suspend fun pinNote(noteId: String): Resource<Note>

    suspend fun unpinNote(noteId: String): Resource<Note>

    suspend fun softDeleteNote(noteId: String): Resource<Note>

    suspend fun restoreNote(noteId: String): Resource<Note>

    suspend fun hardDeleteNote(noteId: String): Resource<Unit>

    suspend fun addTagToNote(noteId: String, tagId: String): Resource<Note>

    suspend fun removeTagFromNote(noteId: String, tagId: String): Resource<Note>

    suspend fun refreshNotes(): Resource<Unit>

    suspend fun createFromTemplate(templateId: String): Resource<Note>

    suspend fun addNoteLink(sourceId: String, targetId: String): Resource<Unit>

    suspend fun removeNoteLink(sourceId: String, targetId: String): Resource<Unit>

    suspend fun getLinkedNotes(noteId: String): Resource<List<Note>>
}

interface TagRepository {

    fun observeTags(): Flow<List<Tag>>

    suspend fun getTags(): Resource<List<Tag>>

    suspend fun createTag(name: String, color: String?): Resource<Tag>

    suspend fun updateTag(tagId: String, name: String, color: String?): Resource<Tag>

    suspend fun deleteTag(tagId: String): Resource<Unit>

    suspend fun refreshTags(): Resource<Unit>
}

interface FolderRepository {

    fun observeFolders(): Flow<List<NoteFolder>>

    suspend fun createFolder(name: String, color: String?, parentFolderId: String?): Resource<NoteFolder>

    suspend fun updateFolder(folderId: String, name: String, color: String?): Resource<NoteFolder>

    suspend fun deleteFolder(folderId: String): Resource<Unit>
}

interface TemplateRepository {

    fun observeTemplates(): Flow<List<NoteTemplate>>

    suspend fun createTemplate(name: String, content: String, icon: String?): Resource<NoteTemplate>

    suspend fun deleteTemplate(templateId: String): Resource<Unit>
}
