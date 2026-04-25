package com.oussama_chatri.productivityx.features.notes.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import kotlinx.coroutines.flow.Flow

// ─── Entities ─────────────────────────────────────────────────────────────────

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val color: String,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    indices = [
        Index("userId"),
        Index("userId", "isDeleted"),
        Index("userId", "updatedAt"),
        Index("syncStatus")
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
    val pendingOperation: String?
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

// ─── Relations ────────────────────────────────────────────────────────────────

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
