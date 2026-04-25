package com.oussama_chatri.productivityx.features.notes.data.mapper

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteTagCrossRef
import com.oussama_chatri.productivityx.features.notes.data.local.NoteWithTags
import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.NoteResponseDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagResponseDto
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
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
    pendingOperation   = null
)

fun NoteResponseDto.toEntityWithRefs(fallbackUserId: String = ""): Pair<NoteEntity, List<NoteTagCrossRef>> {
    val entity = toEntity(fallbackUserId)
    val refs   = tags.map { NoteTagCrossRef(noteId = id, tagId = it.id) }
    return entity to refs
}

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
    createdAt          = parseInstantOrNow(createdAt),
    updatedAt          = parseInstantOrNow(updatedAt)
)


fun TagEntity.toDomain() = Tag(
    id        = id,
    userId    = userId,
    name      = name,
    color     = color,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun NoteWithTags.toDomain() = Note(
    id                 = note.id,
    userId             = note.userId,
    title              = note.title,
    content            = note.content,
    plainTextContent   = note.plainTextContent,
    wordCount          = note.wordCount,
    readingTimeSeconds = note.readingTimeSeconds,
    isPinned           = note.isPinned,
    isDeleted          = note.isDeleted,
    deletedAt          = note.deletedAt?.let { Instant.ofEpochMilli(it) },
    version            = note.version,
    syncStatus         = note.syncStatus,
    tags               = tags.map { it.toDomain() }.toSet(),
    createdAt          = Instant.ofEpochMilli(note.createdAt),
    updatedAt          = Instant.ofEpochMilli(note.updatedAt)
)