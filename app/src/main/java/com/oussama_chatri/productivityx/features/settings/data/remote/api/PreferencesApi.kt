package com.oussama_chatri.productivityx.features.settings.data.remote.api

import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.features.settings.data.remote.dto.request.UpdatePreferencesRequest
import com.oussama_chatri.productivityx.features.settings.data.remote.dto.response.UserPreferencesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PreferencesApi {

    @GET("api/v1/preferences")
    suspend fun getPreferences(): Response<ApiResponse<UserPreferencesResponseDto>>

    @PUT("api/v1/preferences")
    suspend fun updatePreferences(
        @Body request: UpdatePreferencesRequest
    ): Response<ApiResponse<UserPreferencesResponseDto>>
}
