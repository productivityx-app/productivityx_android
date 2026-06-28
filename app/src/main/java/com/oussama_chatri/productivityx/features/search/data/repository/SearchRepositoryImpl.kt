package com.oussama_chatri.productivityx.features.search.data.repository

import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.search.data.remote.api.SearchApiService
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResultType
import com.oussama_chatri.productivityx.features.search.domain.repository.SearchRepository
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApiService,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val eventDao: EventDao,
    private val prefs: PreferencesDataStore
) : SearchRepository {

    override suspend fun search(query: String, types: Set<String>?): Resource<List<SearchResult>> {
        if (query.isBlank()) return Resource.Success(emptyList())
        return when (val result = safeApiCall {
            api.search(query, types?.joinToString(","), 20)
        }) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val body = response.body()
                    Resource.Success(body?.data?.results?.mapNotNull { it.toDomain() } ?: emptyList())
                } else {
                    Resource.Error("Search failed: ${response.code()}")
                }
            }
            is Resource.Error -> Resource.Error(result.message, result.code)
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun searchLocal(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        val userId = prefs.cachedUserId.first() ?: return emptyList()
        val results = mutableListOf<SearchResult>()

        noteDao.searchNotes(userId, query).forEach { entity ->
            results.add(
                SearchResult(
                    id = entity.id,
                    type = SearchResultType.NOTE,
                    title = entity.title,
                    snippet = entity.plainTextContent.take(150),
                    updatedAt = Instant.ofEpochMilli(entity.updatedAt).toString()
                )
            )
        }

        taskDao.searchTasks(userId, query).forEach { entity ->
            results.add(
                SearchResult(
                    id = entity.id,
                    type = SearchResultType.TASK,
                    title = entity.title,
                    snippet = entity.description?.take(150) ?: "",
                    updatedAt = entity.updatedAt.toString()
                )
            )
        }

        eventDao.searchEvents(userId, query).forEach { entity ->
            results.add(
                SearchResult(
                    id = entity.id,
                    type = SearchResultType.EVENT,
                    title = entity.title,
                    snippet = entity.description?.take(150) ?: "",
                    updatedAt = Instant.ofEpochMilli(entity.updatedAt).toString()
                )
            )
        }

        return results.sortedByDescending { it.updatedAt }
    }
}
