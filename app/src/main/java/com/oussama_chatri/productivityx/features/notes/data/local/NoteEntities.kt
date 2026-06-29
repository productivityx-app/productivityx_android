package com.oussama_chatri.productivityx.features.notes.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.oussama_chatri.productivityx.core.enums.SyncStatus

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val color: String,
    val createdAt: Long
)

@Entity(
    tableName = "note_folders",
    indices = [Index("userId"), Index("parentFolderId")]
)
data class FolderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val parentFolderId: String? = null,
    val color: String = "#6366F1",
    val createdAt: Long
)

@Entity(
    tableName = "note_templates",
    indices = [Index("userId")]
)
data class TemplateEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val content: String,
    val icon: String = "note",
    val createdAt: Long
)

@Entity(
    tableName = "note_links",
    primaryKeys = ["sourceNoteId", "targetNoteId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceNoteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sourceNoteId"), Index("targetNoteId")]
)
data class NoteLinkEntity(
    val sourceNoteId: String,
    val targetNoteId: String,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    indices = [
        Index("userId"),
        Index("userId", "isDeleted"),
        Index("userId", "updatedAt"),
        Index("syncStatus"),
        Index("folderId")
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val wordCount: Int,
    val readingTimeSeconds: Int,
    val isPinned: Boolean,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val version: Int,
    val syncStatus: SyncStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val folderId: String? = null,
    val imageUrls: String = "",
    val hasVoiceMemo: Boolean = false,
    val hasFileAttachment: Boolean = false,
    val linkedNoteIds: String = "",
    val pendingOperation: String? = null
)

@Entity(
    tableName = "note_tags",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("tagId")]
)
data class NoteTagCrossRef(
    val noteId: String,
    val tagId: String
)

data class NoteWithTags(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class FolderWithNotes(
    @Embedded val folder: FolderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "folderId"
    )
    val notes: List<NoteEntity>
)
