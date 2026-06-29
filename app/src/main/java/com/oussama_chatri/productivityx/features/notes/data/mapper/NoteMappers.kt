package com.oussama_chatri.productivityx.features.notes.data.mapper

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.features.notes.data.local.FolderEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteTagCrossRef
import com.oussama_chatri.productivityx.features.notes.data.local.NoteWithTags
import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
import com.oussama_chatri.productivityx.features.notes.data.local.TemplateEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteLinkEntity
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagResponseDto
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteLink
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import java.time.Instant

private fun parseInstantOrNow(value: String?): Instant =
    if (value.isNullOrBlank()) Instant.now()
    else runCatching { Instant.parse(value) }.getOrDefault(Instant.now())

private fun parseInstantOrNull(value: String?): Instant? =
    if (value.isNullOrBlank()) null
    else runCatching { Instant.parse(value) }.getOrNull()

private fun parseSyncStatus(value: String?): SyncStatus =
    runCatching { SyncStatus.valueOf(value ?: "") }.getOrDefault(SyncStatus.SYNCED)

fun TagResponseDto.toEntity() = TagEntity(
    id        = id,
    userId    = userId.orEmpty(),
    name      = name,
    color     = color,
    createdAt = parseInstantOrNow(createdAt).toEpochMilli()
)

fun TagResponseDto.toDomain() = Tag(
    id        = id,
    userId    = userId.orEmpty(),
    name      = name,
    color     = color,
    createdAt = parseInstantOrNow(createdAt)
)

fun TagEntity.toDomain() = Tag(
    id        = id,
    userId    = userId,
    name      = name,
    color     = color,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun NoteResponseDto.toEntity(fallbackUserId: String = "") = NoteEntity(
    id                 = id,
    userId             = userId.orEmpty().ifBlank { fallbackUserId },
    title              = title,
    content            = content,
    plainTextContent   = plainTextContent,
    wordCount          = wordCount,
    readingTimeSeconds = readingTimeSeconds,
    isPinned           = pinned,
    isDeleted          = deleted,
    deletedAt          = parseInstantOrNull(deletedAt)?.toEpochMilli(),
    version            = version,
    syncStatus         = parseSyncStatus(syncStatus),
    createdAt          = parseInstantOrNow(createdAt).toEpochMilli(),
    updatedAt          = parseInstantOrNow(updatedAt).toEpochMilli(),
    imageUrls          = "",
    hasVoiceMemo       = false,
    hasFileAttachment  = false,
    linkedNoteIds      = "",
    pendingOperation   = null
)

fun NoteResponseDto.toEntityWithRefs(fallbackUserId: String = ""): Pair<NoteEntity, List<NoteTagCrossRef>> {
    val entity = toEntity(fallbackUserId)
    val refs   = tags.map { TagResponseDto::toEntity }.let { _ ->
        tags.map { tagDto ->
            NoteTagCrossRef(noteId = id, tagId = tagDto.id)
        }
    }
    return entity to refs
}

fun NoteEntity.toDomainWithTags(tags: List<TagEntity> = emptyList()) = Note(
    id                 = id,
    userId             = userId,
    title              = title,
    content            = content,
    plainTextContent   = plainTextContent,
    wordCount          = wordCount,
    readingTimeSeconds = readingTimeSeconds,
    isPinned           = isPinned,
    isDeleted          = isDeleted,
    deletedAt          = deletedAt?.let { Instant.ofEpochMilli(it) },
    version            = version,
    syncStatus         = syncStatus,
    tags               = tags.map { it.toDomain() }.toSet(),
    folderId           = folderId,
    imageUrls          = if (imageUrls.isBlank()) emptyList() else imageUrls.split(",").filter { it.isNotBlank() },
    hasVoiceMemo       = hasVoiceMemo,
    hasFileAttachment  = hasFileAttachment,
    linkedNoteIds      = if (linkedNoteIds.isBlank()) emptyList() else linkedNoteIds.split(",").filter { it.isNotBlank() },
    createdAt          = Instant.ofEpochMilli(createdAt),
    updatedAt          = Instant.ofEpochMilli(updatedAt)
)

fun NoteWithTags.toDomain() = note.toDomainWithTags(tags)

fun NoteResponseDto.toDomain(fallbackUserId: String = "") = Note(
    id                 = id,
    userId             = userId.orEmpty().ifBlank { fallbackUserId },
    title              = title,
    content            = content,
    plainTextContent   = plainTextContent,
    wordCount          = wordCount,
    readingTimeSeconds = readingTimeSeconds,
    isPinned           = pinned,
    isDeleted          = deleted,
    deletedAt          = parseInstantOrNull(deletedAt),
    version            = version,
    syncStatus         = parseSyncStatus(syncStatus),
    tags               = tags.map { it.toDomain() }.toSet(),
    folderId           = null,
    imageUrls          = emptyList(),
    hasVoiceMemo       = false,
    hasFileAttachment  = false,
    linkedNoteIds      = emptyList(),
    createdAt          = parseInstantOrNow(createdAt),
    updatedAt          = parseInstantOrNow(updatedAt)
)

fun FolderEntity.toDomain(noteCount: Int = 0) = NoteFolder(
    id              = id,
    userId          = userId,
    name            = name,
    parentFolderId  = parentFolderId,
    color           = color,
    noteCount       = noteCount,
    createdAt       = Instant.ofEpochMilli(createdAt)
)

fun TemplateEntity.toDomain() = NoteTemplate(
    id        = id,
    userId    = userId,
    name      = name,
    content   = content,
    icon      = icon,
    createdAt = Instant.ofEpochMilli(createdAt)
)
