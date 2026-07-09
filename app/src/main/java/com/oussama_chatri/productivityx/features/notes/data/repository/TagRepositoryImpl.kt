package com.oussama_chatri.productivityx.features.notes.data.repository

import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.notes.data.mapper.toEntity
import com.oussama_chatri.productivityx.features.notes.data.remote.TagApi
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagResponseDto
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.domain.repository.TagRepository
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
class TagRepositoryImpl @Inject constructor(
    private val tagApi: TagApi,
    private val tagDao: TagDao,
    private val preferencesDataStore: PreferencesDataStore
) : TagRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeTags(): Flow<List<Tag>> =
        preferencesDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            tagDao.observeTags(userId).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getTags(): Resource<List<Tag>> {
        val userId = cachedUserId()
        val local  = tagDao.getTags(userId)
        if (local.isNotEmpty()) return Resource.Success(local.map { it.toDomain() })

        val result = safeApiCall { tagApi.listTags() }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val tags = response.body()?.data ?: emptyList()
                    tagDao.upsertAll(tags.map { it.toEntity() })
                    Resource.Success(tags.map { it.toDomain() })
                } else Resource.Error("Failed to fetch tags")
            }
            is Resource.Error   -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun createTag(name: String, color: String?): Resource<Tag> {
        val userId = cachedUserId()
        val tagEntity = TagEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name.trim(),
            color = color ?: "#6750A4",
            createdAt = Instant.now().toEpochMilli()
        )
        tagDao.upsert(tagEntity)

        if (isSyncEnabled()) {
            val result = safeApiCall { tagApi.createTag(TagRequestDto(name, color)) }
            if (result is Resource.Success && result.data.isSuccessful) {
                val dto = result.data.body()?.data
                if (dto != null) {
                    tagDao.deleteById(tagEntity.id)
                    tagDao.upsert(dto.toEntity())
                    return Resource.Success(dto.toDomain())
                }
            }
        }

        return Resource.Success(tagEntity.toDomain())
    }

    override suspend fun updateTag(tagId: String, name: String, color: String?): Resource<Tag> {
        if (isSyncEnabled()) {
            val result = safeApiCall { tagApi.updateTag(tagId, TagRequestDto(name, color)) }
            return handleTagResponse(result) { dto -> tagDao.upsert(dto.toEntity()) }
        }
        val existing = tagDao.getTagById(tagId) ?: return Resource.Error("Tag not found")
        val updated = existing.copy(name = name.trim(), color = color ?: existing.color)
        tagDao.upsert(updated)
        return Resource.Success(updated.toDomain())
    }

    override suspend fun deleteTag(tagId: String): Resource<Unit> {
        tagDao.deleteById(tagId)
        if (isSyncEnabled()) {
            val result = safeApiCall { tagApi.deleteTag(tagId) }
            return when (result) {
                is Resource.Success -> if (result.data.isSuccessful) Resource.Success(Unit)
                else Resource.Error("Failed to delete tag")
                is Resource.Error   -> result
                is Resource.Loading -> Resource.Loading
            }
        }
        return Resource.Success(Unit)
    }

    override suspend fun refreshTags(): Resource<Unit> {
        if (!isSyncEnabled()) return Resource.Success(Unit)
        val result = safeApiCall { tagApi.listTags() }
        if (result is Resource.Success && result.data.isSuccessful) {
            val tags = result.data.body()?.data ?: emptyList()
            tagDao.upsertAll(tags.map { it.toEntity() })
            return Resource.Success(Unit)
        }
        return Resource.Error("Failed to refresh tags")
    }

    private suspend fun handleTagResponse(
        result: Resource<retrofit2.Response<ApiResponse<TagResponseDto>>>,
        onSuccess: suspend (TagResponseDto) -> Unit
    ): Resource<Tag> = when (result) {
        is Resource.Success -> {
            val response = result.data
            if (response.isSuccessful) {
                val dto = response.body()?.data ?: return Resource.Error("Empty response")
                onSuccess(dto)
                Resource.Success(dto.toDomain())
            } else Resource.Error(parseError(response.errorBody()?.string()))
        }
        is Resource.Error   -> result
        is Resource.Loading -> Resource.Loading
    }

    private suspend fun cachedUserId(): String =
        preferencesDataStore.cachedUserId.first() ?: ""

    private suspend fun isSyncEnabled(): Boolean = preferencesDataStore.isSyncEnabled()

    private fun parseError(body: String?): String {
        if (body.isNullOrBlank()) return "Something went wrong."
        return runCatching {
            Regex("\"message\":\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: "Something went wrong."
        }.getOrDefault("Something went wrong.")
    }
}
