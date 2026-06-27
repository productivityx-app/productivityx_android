package com.oussama_chatri.productivityx.features.search.data.remote.api

import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.features.search.data.remote.dto.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("api/v1/search")
    suspend fun search(
        @Query("q")     query: String,
        @Query("types") types: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SearchResponseDto>>
}
