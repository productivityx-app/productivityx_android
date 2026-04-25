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
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val preview: String get() = plainTextContent.take(120).trim()

    val readingTimeLabel: String get() = when {
        readingTimeSeconds < 60 -> "< 1 min read"
        readingTimeSeconds < 3600 -> "${readingTimeSeconds / 60} min read"
        else -> "${readingTimeSeconds / 3600}h read"
    }
}
