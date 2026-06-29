package com.oussama_chatri.productivityx.features.notes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(folders: List<FolderEntity>)

    @Query("DELETE FROM note_folders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM note_folders WHERE userId = :userId ORDER BY name ASC")
    fun observeFolders(userId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM note_folders WHERE userId = :userId ORDER BY name ASC")
    suspend fun getFolders(userId: String): List<FolderEntity>

    @Query("SELECT * FROM note_folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT COUNT(*) FROM notes WHERE folderId = :folderId AND isDeleted = 0")
    suspend fun countNotesInFolder(folderId: String): Long
}
