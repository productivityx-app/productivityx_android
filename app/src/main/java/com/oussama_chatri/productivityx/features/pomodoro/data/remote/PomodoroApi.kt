package com.oussama_chatri.productivityx.features.pomodoro.data.remote

import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.ApiResponse
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.EndSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.InterruptSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.StartSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response.PagedSessionsResponseDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response.PomodoroSessionResponseDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.response.PomodoroStatsResponseDto
import retrofit2.Response
import retrofit2.http.*

interface PomodoroApi {

    @POST(ApiConstants.Pomodoro.START)
    suspend fun startSession(
        @Body request: StartSessionRequestDto
    ): Response<ApiResponse<PomodoroSessionResponseDto>>

    @PATCH
    suspend fun endSession(
        @Url url: String,
        @Body request: EndSessionRequestDto
    ): Response<ApiResponse<PomodoroSessionResponseDto>>

    @PATCH
    suspend fun interruptSession(
        @Url url: String,
        @Body request: InterruptSessionRequestDto
    ): Response<ApiResponse<PomodoroSessionResponseDto>>

    @GET("${ApiConstants.Pomodoro.SESSIONS}/active")
    suspend fun getActiveSession(): Response<ApiResponse<PomodoroSessionResponseDto?>>

    @GET
    suspend fun getSessionById(
        @Url url: String
    ): Response<ApiResponse<PomodoroSessionResponseDto>>

    @GET(ApiConstants.Pomodoro.SESSIONS)
    suspend fun getSessions(
        @Query("page")   page:   Int = 0,
        @Query("size")   size:   Int = 20,
        @Query("taskId") taskId: String? = null
    ): Response<ApiResponse<PagedSessionsResponseDto>>

    @GET("api/v1/pomodoro/stats/today")
    suspend fun getTodayStats(): Response<ApiResponse<PomodoroStatsResponseDto>>
}

