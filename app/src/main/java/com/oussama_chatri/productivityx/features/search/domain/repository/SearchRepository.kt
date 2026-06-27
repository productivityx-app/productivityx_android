package com.oussama_chatri.productivityx.features.search.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult

interface SearchRepository {
    suspend fun search(query: String, types: Set<String>? = null): Resource<List<SearchResult>>
    suspend fun searchLocal(query: String): List<SearchResult>
}
