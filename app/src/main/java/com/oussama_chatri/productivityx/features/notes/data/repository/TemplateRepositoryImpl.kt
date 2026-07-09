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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val preferencesDataStore: PreferencesDataStore
) : TemplateRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeTemplates(): Flow<List<NoteTemplate>> =
        preferencesDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            templateDao.observeTemplates(userId).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createTemplate(name: String, content: String, icon: String?): Resource<NoteTemplate> {
        val userId = cachedUserId()
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

    private suspend fun cachedUserId(): String =
        preferencesDataStore.cachedUserId.first() ?: ""
}
