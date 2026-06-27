package com.oussama_chatri.productivityx.features.search.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResultType

data class SearchResponseDto(
    @SerializedName("results") val results: List<SearchResultDto>,
    @SerializedName("total")   val total: Int,
    @SerializedName("query")   val query: String
) {
    fun toDomain(): com.oussama_chatri.productivityx.features.search.domain.model.SearchResponse =
        com.oussama_chatri.productivityx.features.search.domain.model.SearchResponse(
            results = results.map { it.toDomain() },
            total = total,
            query = query
        )
}

data class SearchResultDto(
    @SerializedName("id")         val id: String,
    @SerializedName("type")       val type: String,
    @SerializedName("title")      val title: String,
    @SerializedName("snippet")    val snippet: String,
    @SerializedName("updatedAt")  val updatedAt: String?
) {
    fun toDomain(): SearchResult = SearchResult(
        id = id,
        type = SearchResultType.valueOf(type),
        title = title,
        snippet = snippet,
        updatedAt = updatedAt
    )
}
