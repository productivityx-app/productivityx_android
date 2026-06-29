package com.oussama_chatri.productivityx.features.notes.data.repository

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteLinkEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteTagCrossRef
import com.oussama_chatri.productivityx.features.notes.data.local.TemplateDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomainWithTags
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntity
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntityWithRefs
import com.oussama_chatri.productivityx.features.notes.data.remote.NoteApi
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.AddTagToNoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.repository.NoteRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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
    private val templateDao: TemplateDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val gson: Gson
) : NoteRepository {

    override fun observeActiveNotes(tagId: String?, pinnedOnly: Boolean, tagIds: List<String>?, folderId: String?): Flow<List<Note>> {
        val userId = cachedUserId()
        val flow = when {
            tagId != null -> noteDao.observeNotesByTag(userId, tagId)
            !tagIds.isNullOrEmpty() -> noteDao.observeNotesByTags(userId, tagIds)
            folderId != null -> noteDao.observeNotesByFolder(userId, folderId)
            pinnedOnly -> noteDao.observePinnedNotes(userId)
            else -> noteDao.observeActiveNotes(userId)
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun getPagedActiveNotes(tagId: String?, pinnedOnly: Boolean, tagIds: List<String>?, folderId: String?): Flow<PagingData<Note>> {
        val userId = cachedUserId()
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true),
            pagingSourceFactory = {
                when {
                    tagId != null -> noteDao.getPagedNotesByTag(userId, tagId)
                    !tagIds.isNullOrEmpty() -> noteDao.getPagedNotesByTags(userId, tagIds)
                    folderId != null -> noteDao.getPagedNotesByFolder(userId, folderId)
                    pinnedOnly -> noteDao.getPagedPinnedNotes(userId)
                    else -> noteDao.getPagedActiveNotes(userId)
                }
            }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override fun observeTrash(): Flow<List<Note>> =
        noteDao.observeTrash(cachedUserId()).map { list -> list.map { it.toDomain() } }

    override fun observeSearch(query: String): Flow<List<Note>> =
        noteDao.observeSearchNotes(cachedUserId(), query).map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(noteId: String): Resource<Note> {
        val userId = cachedUserIdSuspend()
        val local = noteDao.getNoteByIdAndUser(noteId, userId)
        if (local != null) return Resource.Success(local.toDomain())

        val result = safeApiCall { noteApi.getNoteById(noteId) }
        return handleNoteResponse(result) { dto ->
            val (entity, refs) = dto.toEntityWithRefs(userId)
            noteDao.upsert(entity)
            noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
        }
    }

    override suspend fun createNote(
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?,
        folderId: String?
    ): Resource<Note> {
        val userId = cachedUserIdSuspend()
        val clientId = UUID.randomUUID().toString()
        val now = Instant.now().toEpochMilli()
        val plain = stripMarkdown(content ?: "")

        val entity = NoteEntity(
            id = clientId,
            userId = userId,
            title = title?.trim() ?: "",
            content = content ?: "",
            plainTextContent = plain,
            wordCount = wordCount(plain),
            readingTimeSeconds = readingTimeSeconds(wordCount(plain)),
            isPinned = pinned == true,
            isDeleted = false,
            deletedAt = null,
            version = 1,
            syncStatus = SyncStatus.PENDING,
            createdAt = now,
            updatedAt = now,
            folderId = folderId,
            pendingOperation = "CREATE"
        )
        noteDao.upsert(entity)
        if (!tagIds.isNullOrEmpty()) noteDao.replaceNoteTags(clientId, tagIds.toList())

        if (isSyncEnabled()) {
            enqueueSync(clientId, SyncOperation.CREATE, gson.toJson(NoteRequestDto(title, content, tagIds, pinned)))

            val remote = safeApiCall { noteApi.createNote(NoteRequestDto(title, content, tagIds, pinned)) }
            if (remote is Resource.Success && remote.data.isSuccessful) {
                val dto = remote.data.body()?.data
                if (dto != null) {
                    noteDao.deleteById(clientId)
                    val (remoteEntity, refs) = dto.toEntityWithRefs(userId)
                    noteDao.upsert(remoteEntity)
                    noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
                    syncQueueDao.deleteByEntity(clientId, EntityType.NOTE)
                    return Resource.Success(dto.toDomain(userId))
                }
            }
        }

        return noteDao.getNoteById(clientId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Failed to create note")
    }

    override suspend fun updateNote(
        noteId: String,
        title: String?,
        content: String?,
        tagIds: Set<String>?,
        pinned: Boolean?,
        folderId: String?
    ): Resource<Note> {
        val userId = cachedUserIdSuspend()
        val now = Instant.now().toEpochMilli()

        var originalVersion: Int? = null
        var originalUpdatedAt: Long? = null

        noteDao.getNoteById(noteId)?.let { local ->
            originalVersion = local.note.version
            originalUpdatedAt = local.note.updatedAt

            val plain = content?.let { stripMarkdown(it) } ?: local.note.plainTextContent
            val updated = local.note.copy(
                title = title?.trim() ?: local.note.title,
                content = content ?: local.note.content,
                plainTextContent = plain,
                wordCount = wordCount(plain),
                readingTimeSeconds = readingTimeSeconds(wordCount(plain)),
                isPinned = pinned ?: local.note.isPinned,
                folderId = folderId ?: local.note.folderId,
                version = local.note.version + 1,
                syncStatus = SyncStatus.PENDING,
                updatedAt = now,
                pendingOperation = "UPDATE"
            )
            noteDao.upsert(updated)
            if (tagIds != null) noteDao.replaceNoteTags(noteId, tagIds.toList())
        }

        if (isSyncEnabled()) {
            val clientUpdatedAt = originalUpdatedAt?.let { Instant.ofEpochMilli(it).toString() }

            enqueueSync(noteId, SyncOperation.UPDATE, gson.toJson(NoteRequestDto(title, content, tagIds, pinned, originalVersion, clientUpdatedAt)))

            val remote = safeApiCall { noteApi.updateNote(noteId, NoteRequestDto(title, content, tagIds, pinned, originalVersion, clientUpdatedAt)) }
            if (remote is Resource.Success && remote.data.isSuccessful) {
                val dto = remote.data.body()?.data
                if (dto != null) {
                    val (remoteEntity, refs) = dto.toEntityWithRefs(userId)
                    noteDao.upsert(remoteEntity)
                    noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
                    syncQueueDao.deleteByEntity(noteId, EntityType.NOTE)
                    return Resource.Success(dto.toDomain(userId))
                }
            }
        }

        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun pinNote(noteId: String): Resource<Note> {
        togglePinnedLocally(noteId, true)
        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.pinNote(noteId) }
            return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserIdSuspend())) }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun unpinNote(noteId: String): Resource<Note> {
        togglePinnedLocally(noteId, false)
        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.unpinNote(noteId) }
            return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserIdSuspend())) }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun softDeleteNote(noteId: String): Resource<Note> {
        val now = Instant.now().toEpochMilli()
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(
                it.note.copy(
                    isDeleted = true,
                    deletedAt = now,
                    isPinned = false,
                    syncStatus = SyncStatus.PENDING,
                    pendingOperation = "DELETE",
                    updatedAt = now
                )
            )
        }
        if (isSyncEnabled()) {
            enqueueSync(noteId, SyncOperation.DELETE, "{}")
            val remote = safeApiCall { noteApi.softDeleteNote(noteId) }
            return handleNoteResponse(remote) { dto ->
                noteDao.upsert(dto.toEntity(cachedUserIdSuspend()))
                syncQueueDao.deleteByEntity(noteId, EntityType.NOTE)
            }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun restoreNote(noteId: String): Resource<Note> {
        val now = Instant.now().toEpochMilli()
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(
                it.note.copy(
                    isDeleted = false,
                    deletedAt = null,
                    syncStatus = SyncStatus.PENDING,
                    pendingOperation = "UPDATE",
                    updatedAt = now
                )
            )
        }
        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.restoreNote(noteId) }
            return handleNoteResponse(remote) { dto -> noteDao.upsert(dto.toEntity(cachedUserIdSuspend())) }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun hardDeleteNote(noteId: String): Resource<Unit> {
        noteDao.deleteById(noteId)
        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.hardDeleteNote(noteId) }
            return when (remote) {
                is Resource.Success -> if (remote.data.isSuccessful) Resource.Success(Unit)
                else Resource.Error(parseError(remote.data.errorBody()?.string()))
                is Resource.Error -> remote
                is Resource.Loading -> Resource.Loading
            }
        }
        return Resource.Success(Unit)
    }

    override suspend fun addTagToNote(noteId: String, tagId: String): Resource<Note> {
        noteDao.insertNoteTagRef(NoteTagCrossRef(noteId, tagId))
        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.addTagToNote(noteId, AddTagToNoteRequestDto(tagId)) }
            return handleNoteResponse(remote) { dto ->
                val (entity, refs) = dto.toEntityWithRefs(cachedUserIdSuspend())
                noteDao.upsert(entity)
                noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
            }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun removeTagFromNote(noteId: String, tagId: String): Resource<Note> {
        val local = noteDao.getNoteById(noteId)
        val remaining = local?.tags?.map { it.id }?.filter { it != tagId } ?: emptyList()
        noteDao.clearTagsForNote(noteId)
        remaining.forEach { noteDao.insertNoteTagRef(NoteTagCrossRef(noteId, it)) }

        if (isSyncEnabled()) {
            val remote = safeApiCall { noteApi.removeTagFromNote(noteId, tagId) }
            return handleNoteResponse(remote) { dto ->
                val (entity, refs) = dto.toEntityWithRefs(cachedUserIdSuspend())
                noteDao.upsert(entity)
                noteDao.replaceNoteTags(dto.id, refs.map { it.tagId })
            }
        }
        return noteDao.getNoteById(noteId)?.let { Resource.Success(it.toDomain()) }
            ?: Resource.Error("Note not found")
    }

    override suspend fun refreshNotes(): Resource<Unit> {
        if (!isSyncEnabled()) return Resource.Success(Unit)
        val userId = cachedUserIdSuspend()
        val result = safeApiCall { noteApi.listActiveNotes(size = 100) }
        if (result is Resource.Success && result.data.isSuccessful) {
            result.data.body()?.data?.content?.forEach { dto ->
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

    override suspend fun createFromTemplate(templateId: String): Resource<Note> {
        val template = templateDao.getTemplateById(templateId) ?: return Resource.Error("Template not found")
        return createNote(
            title = template.name,
            content = template.content,
            tagIds = null,
            pinned = null,
            folderId = null
        )
    }

    override suspend fun addNoteLink(sourceId: String, targetId: String): Resource<Unit> {
        noteDao.insertNoteLink(NoteLinkEntity(sourceId, targetId, Instant.now().toEpochMilli()))
        return Resource.Success(Unit)
    }

    override suspend fun removeNoteLink(sourceId: String, targetId: String): Resource<Unit> {
        noteDao.deleteNoteLink(sourceId, targetId)
        return Resource.Success(Unit)
    }

    override suspend fun getLinkedNotes(noteId: String): Resource<List<Note>> {
        val linked = noteDao.getLinkedNotes(noteId)
        return Resource.Success(linked.map { it.toDomain() })
    }

    private suspend fun togglePinnedLocally(noteId: String, pinned: Boolean) {
        noteDao.getNoteById(noteId)?.let {
            noteDao.upsert(it.note.copy(isPinned = pinned, updatedAt = Instant.now().toEpochMilli()))
        }
    }

    private suspend fun enqueueSync(entityId: String, operation: SyncOperation, payload: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = EntityType.NOTE,
                entityId = entityId,
                operation = operation,
                payload = payload
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
                    Resource.Success(dto.toDomain(cachedUserIdSuspend()))
                } else Resource.Error("Empty response body")
            } else Resource.Error(parseError(response.errorBody()?.string()))
        }
        is Resource.Error -> result
        is Resource.Loading -> Resource.Loading
    }

    private fun cachedUserId(): String =
        runCatching { runBlocking { preferencesDataStore.cachedUserId.first() ?: "" } }.getOrDefault("")

    private suspend fun cachedUserIdSuspend(): String =
        preferencesDataStore.cachedUserId.first() ?: ""

    private suspend fun isSyncEnabled(): Boolean = preferencesDataStore.isSyncEnabled()

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
