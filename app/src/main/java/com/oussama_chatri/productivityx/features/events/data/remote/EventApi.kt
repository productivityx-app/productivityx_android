package com.oussama_chatri.productivityx.features.events.data.remote

import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventRequestDto
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventResponseDto
import com.oussama_chatri.productivityx.features.notes.data.remote.dto.PagedResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {

    @POST("api/v1/events")
    suspend fun createEvent(
        @Body request: EventRequestDto
    ): Response<ApiResponse<EventResponseDto>>

    @GET("api/v1/events/{id}")
    suspend fun getEventById(
        @Path("id") id: String
    ): Response<ApiResponse<EventResponseDto>>

    @GET("api/v1/events")
    suspend fun listEvents(
        @Query("from") from: String,
        @Query("to")   to: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<PagedResponseDto<EventResponseDto>>>

    @PUT("api/v1/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: EventRequestDto
    ): Response<ApiResponse<EventResponseDto>>

    @DELETE("api/v1/events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): Response<ApiResponse<EventResponseDto>>

    @PATCH("api/v1/events/{id}/restore")
    suspend fun restoreEvent(
        @Path("id") id: String
    ): Response<ApiResponse<EventResponseDto>>
}
