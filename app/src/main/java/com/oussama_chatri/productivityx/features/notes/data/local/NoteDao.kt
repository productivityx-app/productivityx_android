package com.oussama_chatri.productivityx.features.notes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :cutoffMs")
    suspend fun purgeOldTrash(cutoffMs: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTagRef(ref: NoteTagCrossRef)

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun clearTagsForNote(noteId: String)

    @Transaction
    suspend fun replaceNoteTags(noteId: String, tagIds: List<String>) {
        clearTagsForNote(noteId)
        tagIds.forEach { insertNoteTagRef(NoteTagCrossRef(noteId, it)) }
    }

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY isPinned DESC, updatedAt DESC
    """
    )
    fun observeActiveNotes(userId: String): Flow<List<NoteWithTags>>

    @Transaction
    @Query(
        """
        SELECT n.* FROM notes n
        INNER JOIN note_tags nt ON n.id = nt.noteId
        WHERE n.userId = :userId AND n.isDeleted = 0 AND nt.tagId = :tagId
        ORDER BY n.isPinned DESC, n.updatedAt DESC
    """
    )
    fun observeNotesByTag(userId: String, tagId: String): Flow<List<NoteWithTags>>

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE userId = :userId AND isDeleted = 0 AND isPinned = 1
        ORDER BY updatedAt DESC
    """
    )
    fun observePinnedNotes(userId: String): Flow<List<NoteWithTags>>

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE userId = :userId AND isDeleted = 1
        ORDER BY deletedAt DESC
    """
    )
    fun observeTrash(userId: String): Flow<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getNoteByIdAndUser(id: String, userId: String): NoteWithTags?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteWithTags?

    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingNotes(userId: String): List<NoteWithTags>

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isDeleted = 0")
    suspend fun countActiveNotes(userId: String): Long

    @Query(
        """
        SELECT * FROM notes
        WHERE userId = :userId AND isDeleted = 0
          AND (LOWER(title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(plainTextContent) LIKE '%' || LOWER(:query) || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """
    )
    suspend fun searchNotes(userId: String, query: String): List<NoteEntity>

    @Query("UPDATE notes SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    // Returns the title of the most recently edited non-deleted note (used for AI context)
    @Query(
        """
        SELECT title FROM notes
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY updatedAt DESC
        LIMIT 1
    """
    )
    suspend fun lastEditedTitle(userId: String): String?
}