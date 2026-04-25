package com.oussama_chatri.productivityx.features.notes.data.remote

import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagRequestDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.TagResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TagApi {

    @POST("api/v1/tags")
    suspend fun createTag(
        @Body request: TagRequestDto
    ): Response<ApiResponse<TagResponseDto>>

    @GET("api/v1/tags")
    suspend fun listTags(): Response<ApiResponse<List<TagResponseDto>>>

    @PUT("api/v1/tags/{id}")
    suspend fun updateTag(
        @Path("id") id: String,
        @Body request: TagRequestDto
    ): Response<ApiResponse<TagResponseDto>>

    @DELETE("api/v1/tags/{id}")
    suspend fun deleteTag(
        @Path("id") id: String
    ): Response<ApiResponse<Void>>
}
