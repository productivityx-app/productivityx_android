package com.oussama_chatri.productivityx.features.notes.data.repository

import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.data.local.TemplateDao
import com.oussama_chatri.productivityx.features.notes.data.local.TemplateEntity
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate
import com.oussama_chatri.productivityx.features.notes.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val preferencesDataStore: PreferencesDataStore
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<NoteTemplate>> {
        val userId = cachedUserId()
        return templateDao.observeTemplates(userId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createTemplate(name: String, content: String, icon: String?): Resource<NoteTemplate> {
        val userId = cachedUserIdSuspend()
        val entity = TemplateEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name.trim(),
            content = content,
            icon = icon ?: "note",
            createdAt = Instant.now().toEpochMilli()
        )
        templateDao.upsert(entity)
        return Resource.Success(entity.toDomain())
    }

    override suspend fun deleteTemplate(templateId: String): Resource<Unit> {
        templateDao.deleteById(templateId)
        return Resource.Success(Unit)
    }

    private fun cachedUserId(): String =
        runCatching { runBlocking { preferencesDataStore.cachedUserId.first() ?: "" } }.getOrDefault("")

    private suspend fun cachedUserIdSuspend(): String =
        preferencesDataStore.cachedUserId.first() ?: ""
}
