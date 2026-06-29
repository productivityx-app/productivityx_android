package com.oussama_chatri.productivityx.features.notes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: TemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(templates: List<TemplateEntity>)

    @Query("DELETE FROM note_templates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM note_templates WHERE userId = :userId ORDER BY name ASC")
    fun observeTemplates(userId: String): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM note_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): TemplateEntity?
}
