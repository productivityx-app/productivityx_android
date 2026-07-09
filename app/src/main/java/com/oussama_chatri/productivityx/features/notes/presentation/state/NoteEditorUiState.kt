package com.oussama_chatri.productivityx.features.notes.presentation.state

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import java.time.Instant

enum class EditorFocusMode { NORMAL, FOCUS, TYPEWRITER }

data class NoteEditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val plainTextContent: String = "",
    val tags: Set<Tag> = emptySet(),
    val imageUrls: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
    val lastSavedAt: Instant? = null,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingTimeSeconds: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val focusMode: EditorFocusMode = EditorFocusMode.NORMAL,
    val showMetadata: Boolean = false,
    val isPreviewMode: Boolean = false,
    val showExportSheet: Boolean = false,
    val error: String? = null,
    val linkedNoteTitles: List<String> = emptyList(),
    val backlinks: List<String> = emptyList()
) {
    val readingTimeLabel: String get() = when {
        readingTimeSeconds < 60 -> "< 1 min read"
        readingTimeSeconds < 3600 -> "${readingTimeSeconds / 60} min read"
        else -> "${readingTimeSeconds / 3600}h read"
    }
}
