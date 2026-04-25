package com.oussama_chatri.productivityx.features.notes.data.repository

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteTagCrossRef
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntity
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntityWithRefs
import com.oussama_chatri.productivityx.features.notes.data.remote.NoteApi
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.AddTagToNoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteApi: NoteApi,
    private val noteDao: NoteDao,
    private val syncQueueDao: SyncQueueDao,
    private val tagDao: TagDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val gson: Gson
) : NoteRepository {

    // Observe

    override fun observeActiveNotes(tagId: String?, pinnedOnly: Boolean): Flow<List<Note>> {
        val userId = cachedUserId()
        return when {
            tagId != null -> noteDao.observeNotesByTag(userId, tagId)
            pinnedOnly    -> noteDao.observePinnedNotes(userId)
            else          -> noteDao.observeActiveNotes(userId)
        }.map { list -> list.map { it.toDomain() } }
    }

    override fun observeTrash(): Flow<List<Note>> =
        noteDao.observeTrash(cachedUserId()).map { list -> list.map { it.toDomain() } }

    // Single fetch

    override suspend fun getNoteById(noteId: String): Resource<Note> {
        val userId = cachedUserId()
        val local  = noteDao.getNoteByIdAndUser(noteId, userId)
        if (local != null) return Resource.Success(local.toDomain())

        val result = safeApiCall { noteApi.getNoteById(noteId) }
        return handleNoteResponse(result) { dto ->
            val (entity, refs) = dto.toEntityWithRefs(userId)
            noteDao.upsert(entity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
        }
    }

    // Create

    override suspend fun createNote(
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?
    ): Resource<Note> {
        val userId   = cachedUserId()
        val clientId = UUID.randomUUID().toString()
        val now      = Instant.now().toEpochMilli()
        val plain    = stripMarkdown(content ?: "")

        // Write locally first — UI reflects the note immediately
        val entity = NoteEntity(
            id                 = clientId,
            userId             = userId,
            title              = title?.trim() ?: "",
            content            = content ?: "",
            plainTextContent   = plain,
            wordCount          = wordCount(plain),
            readingTimeSeconds = readingTimeSeconds(wordCount(plain)),
            isPinned           = pinned == true,
            isDeleted          = false,
            deletedAt          = null,
            version            = 1,
            syncStatus         = SyncStatus.PENDING,
            createdAt          = now,
            updatedAt          = now,
            pendingOperation   = "CREATE"
        )
        noteDao.upsert(entity)
        if (!tagIds.isNullOrEmpty()) noteDao.replaceNoteTags(clientId, tagIds.toList())

        enqueueSync(clientId, SyncOperation.CREATE, gson.toJson(NoteRequestDto(title, content, tagIds, pinned)))

        // Sync to remote and replace client-side temp record with server record
        val remote = safeApiCall { noteApi.createNote(NoteRequestDto(title, content, tagIds, pinned)) }
        return handleNoteResponse(remote) { dto ->
            noteDao.deleteById(clientId)
            val (remoteEntity, refs) = dto.toEntityWithRefs(userId)   // <-- fallbackUserId
            noteDao.upsert(remoteEntity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
            syncQueueDao.deleteByEntity(clientId, EntityType.NOTE)
        }
    }

    // Update

    override suspend fun updateNote(
        noteId: String,
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?
    ): Resource<Note> {
        val userId = cachedUserId()
        val now    = Instant.now().toEpochMilli()

        noteDao.getNoteById(noteId)?.let { local ->
            val plain   = content?.let { stripMarkdown(it) } ?: local.note.plainTextContent
            val updated = local.note.copy(
                title              = title?.trim() ?: local.note.title,
                content            = content ?: local.note.content,
                plainTextContent   = plain,
                wordCount          = wordCount(plain),
                readingTimeSeconds = readingTimeSeconds(wordCount(plain)),
                isPinned           = pinned ?: local.note.isPinned,
                version            = local.note.version + 1,
                syncStatus         = SyncStatus.PENDING,
                updatedAt          = now,
                pendingOperation   = "UPDATE"
            )
            noteDao.upsert(updated)
            if (tagIds != null) noteDao.replaceNoteTags(noteId, tagIds.toList())
        }

        enqueueSync(noteId, SyncOperation.UPDATE, gson.toJson(NoteRequestDto(title, content, tagIds, pinned)))

        val remote = safeApiCall { noteApi.updateNote(noteId, NoteRequestDto(title, content, tagIds, pinned)) }
        return handleNoteResponse(remote) { dto ->
            val (remoteEntity, refs) = dto.toEntityWithRefs(userId)
            noteDao.upsert(remoteEntity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
            syncQueueDao.deleteByEntity(noteId, EntityType.NOTE)
        }
    }

    // Pin / Unpin

    override suspend fun pinNote(noteId: String): Resource<Note> {
        togglePinnedLocally(noteId, true)
        val remote = safeApiCall { noteApi.pinNote(noteId) }
        return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserId())) }
    }

    override suspend fun unpinNote(noteId: String): Resource<Note> {
        togglePinnedLocally(noteId, false)
        val remote = safeApiCall { noteApi.unpinNote(noteId) }
        return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserId())) }
    }

    // Delete / Restore

    override suspend fun softDeleteNote(noteId: String): Resource<Note> {
        val now = Instant.now().toEpochMilli()
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(
                it.note.copy(
                    isDeleted        = true,
                    deletedAt        = now,
                    isPinned         = false,
                    syncStatus       = SyncStatus.PENDING,
                    pendingOperation = "DELETE",
                    updatedAt        = now
                )
            )
        }
        enqueueSync(noteId, SyncOperation.DELETE, "{}")
        val remote = safeApiCall { noteApi.softDeleteNote(noteId) }
        return handleNoteResponse(remote) { dto ->
            noteDao.upsert(dto.toEntity(cachedUserId()))
            syncQueueDao.deleteByEntity(noteId, EntityType.NOTE)
        }
    }

    override suspend fun restoreNote(noteId: String): Resource<Note> {
        val now = Instant.now().toEpochMilli()
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(
                it.note.copy(
                    isDeleted        = false,
                    deletedAt        = null,
                    syncStatus       = SyncStatus.PENDING,
                    pendingOperation = "UPDATE",
                    updatedAt        = now
                )
            )
        }
        val remote = safeApiCall { noteApi.restoreNote(noteId) }
        return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserId())) }
    }

    override suspend fun hardDeleteNote(noteId: String): Resource<Unit> {
        noteDao.deleteById(noteId)
        val remote = safeApiCall { noteApi.hardDeleteNote(noteId) }
        return when (remote) {
            is Resource.Success -> if (remote.data.isSuccessful) Resource.Success(Unit)
            else Resource.Error(parseError(remote.data.errorBody()?.string()))
            is Resource.Error   -> remote
            is Resource.Loading -> Resource.Loading
        }
    }

    // Tags

    override suspend fun addTagToNote(noteId: String, tagId: String): Resource<Note> {
        noteDao.insertNoteTagRef(NoteTagCrossRef(noteId, tagId))
        val remote = safeApiCall { noteApi.addTagToNote(noteId, AddTagToNoteRequestDto(tagId)) }
        return handleNoteResponse(remote) { dto ->
            val (entity, refs) = dto.toEntityWithRefs(cachedUserId())
            noteDao.upsert(entity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
        }
    }

    override suspend fun removeTagFromNote(noteId: String, tagId: String): Resource<Note> {
        val local     = noteDao.getNoteById(noteId)
        val remaining = local?.tags?.map { it.id }?.filter { it != tagId } ?: emptyList()
        noteDao.clearTagsForNote(noteId)
        remaining.forEach { noteDao.insertNoteTagRef(NoteTagCrossRef(noteId, it)) }

        val remote = safeApiCall { noteApi.removeTagFromNote(noteId, tagId) }
        return handleNoteResponse(remote) { dto ->
            val (entity, refs) = dto.toEntityWithRefs(cachedUserId())
            noteDao.upsert(entity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
        }
    }

    // Refresh

    override suspend fun refreshNotes(): Resource<Unit> {
        val userId = cachedUserId()
        val result = safeApiCall { noteApi.listActiveNotes(size = 100) }
        if (result is Resource.Success && result.data.isSuccessful) {
            result.data.body()?.data?.content?.forEach { dto ->
                // Upsert tags before inserting cross-refs
                if (dto.tags.isNotEmpty()) {
                    tagDao.upsertAll(dto.tags.map { it.toEntity() })
                }
                val (entity, refs) = dto.toEntityWithRefs(userId)
                noteDao.upsert(entity)
                noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
            }
            return Resource.Success(Unit)
        }
        return Resource.Error("Failed to refresh notes")
    }

    // Helpers

    private suspend fun togglePinnedLocally(noteId: String, pinned: Boolean) {
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(it.note.copy(isPinned = pinned, updatedAt = Instant.now().toEpochMilli()))
        }
    }

    private suspend fun enqueueSync(entityId: String, operation: SyncOperation, payload: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id         = UUID.randomUUID().toString(),
                entityType = EntityType.NOTE,
                entityId   = entityId,
                operation  = operation,
                payload    = payload
            )
        )
    }

    private suspend fun handleNoteResponse(
        result: Resource<Response<ApiResponse<NoteResponseDto>>>,
        onSuccess: suspend (NoteResponseDto) -> Unit
    ): Resource<Note> = when (result) {
        is Resource.Success -> {
            val response = result.data
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    onSuccess(dto)
                    Resource.Success(dto.toDomain(cachedUserId()))
                } else Resource.Error("Empty response body")
            } else Resource.Error(parseError(response.errorBody()?.string()))
        }
        is Resource.Error   -> result
        is Resource.Loading -> Resource.Loading
    }

    private fun cachedUserId(): String =
        runCatching { runBlocking { preferencesDataStore.cachedUserId.first() ?: "" } }.getOrDefault("")

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Something went wrong."
        return runCatching {
            Regex("\"message\":\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: "Something went wrong."
        }.getOrDefault("Something went wrong.")
    }

    private fun stripMarkdown(md: String): String {
        val pattern = Regex(
            "(!?\\[([^\\]]*)\\]\\([^)]*\\))" +
                    "|(```[\\s\\S]*?```)" +
                    "|(`.+?`)" +
                    "|(^#{1,6}\\s)" +
                    "|(\\*{1,3}|_{1,3})" +
                    "|(~~.+?~~)" +
                    "|(^[-*+]\\s|^\\d+\\.\\s)" +
                    "|(^>+\\s?)" +
                    "|(^---+$|^===+$)" +
                    "|(\\n{2,})"
        )
        return Regex("\\s{2,}").replace(pattern.replace(md, " "), " ").trim()
    }

    private fun wordCount(plain: String): Int =
        if (plain.isBlank()) 0 else plain.trim().split(Regex("\\s+")).size

    private fun readingTimeSeconds(words: Int): Int =
        if (words == 0) 0 else (words.toDouble() / 200.0 * 60).toInt().coerceAtLeast(1)
}