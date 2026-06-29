package com.oussama_chatri.productivityx.features.notes.domain.model

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import java.time.Instant

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val wordCount: Int,
    val readingTimeSeconds: Int,
    val isPinned: Boolean,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val version: Int,
    val syncStatus: SyncStatus,
    val tags: Set<Tag>,
    val folderId: String? = null,
    val imageUrls: List<String> = emptyList(),
    val hasVoiceMemo: Boolean = false,
    val hasFileAttachment: Boolean = false,
    val linkedNoteIds: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val preview: String get() = plainTextContent.take(200).trim()

    val firstThreeLines: String get() {
        val lines = plainTextContent.lines().filter { it.isNotBlank() }
        return lines.take(3).joinToString("\n").take(200).trim()
    }

    val readingTimeLabel: String get() = when {
        readingTimeSeconds < 60 -> "< 1 min read"
        readingTimeSeconds < 3600 -> "${readingTimeSeconds / 60} min read"
        else -> "${readingTimeSeconds / 3600}h read"
    }
}

data class NoteFolder(
    val id: String,
    val userId: String,
    val name: String,
    val parentFolderId: String? = null,
    val color: String = "#6366F1",
    val noteCount: Int = 0,
    val createdAt: Instant
)

data class NoteTemplate(
    val id: String,
    val userId: String,
    val name: String,
    val content: String,
    val icon: String = "note",
    val createdAt: Instant
)

data class NoteLink(
    val sourceNoteId: String,
    val targetNoteId: String,
    val targetNoteTitle: String = "",
    val createdAt: Instant = Instant.now()
)
