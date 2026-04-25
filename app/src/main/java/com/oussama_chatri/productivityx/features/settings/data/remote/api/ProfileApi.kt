package com.oussama_chatri.productivityx.features.profile.data.remote.api

import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.request.UpdateAvatarRequest
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.request.UpdateProfileRequest
import com.oussama_chatri.productivityx.features.profile.data.remote.dto.response.ProfileResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface ProfileApi {

    @GET("api/v1/profile")
    suspend fun getProfile(): Response<ApiResponse<ProfileResponseDto>>

    @PUT("api/v1/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<ProfileResponseDto>>

    @PATCH("api/v1/profile/avatar")
    suspend fun updateAvatar(
        @Body request: UpdateAvatarRequest
    ): Response<ApiResponse<ProfileResponseDto>>
}
