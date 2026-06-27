package com.oussama_chatri.productivityx.features.search.domain.model

enum class SearchResultType { NOTE, TASK, EVENT }

data class SearchResult(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val snippet: String,
    val updatedAt: String?
)

data class SearchResponse(
    val results: List<SearchResult>,
    val total: Int,
    val query: String
)
