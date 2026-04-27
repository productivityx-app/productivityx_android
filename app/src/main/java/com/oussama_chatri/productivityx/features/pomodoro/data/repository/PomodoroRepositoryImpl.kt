package com.oussama_chatri.productivityx.features.pomodoro.data.repository

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.PomodoroApi
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.EndSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.InterruptSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request.StartSessionRequestDto
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepositoryImpl @Inject constructor(
    private val api: PomodoroApi
) : PomodoroRepository {

    override suspend fun startSession(
        type: PomodoroType,
        taskId: String?
    ): Resource<PomodoroSession> {
        val result = safeApiCall {
            api.startSession(StartSessionRequestDto(type = type, taskId = taskId))
        }
        return mapResponse(result) { it.toDomain() }
    }

    override suspend fun endSession(
        sessionId: String,
        actualDurationSeconds: Int?
    ): Resource<PomodoroSession> {
        val result = safeApiCall {
            api.endSession(
                url     = ApiConstants.Pomodoro.end(sessionId),
                request = EndSessionRequestDto(actualDurationSeconds)
            )
        }
        return mapResponse(result) { it.toDomain() }
    }

    override suspend fun interruptSession(
        sessionId: String,
        actualDurationSeconds: Int?,
        reason: String?
    ): Resource<PomodoroSession> {
        val result = safeApiCall {
            api.interruptSession(
                url     = ApiConstants.Pomodoro.interrupt(sessionId),
                request = InterruptSessionRequestDto(actualDurationSeconds, reason)
            )
        }
        return mapResponse(result) { it.toDomain() }
    }

    override suspend fun getActiveSession(): Resource<PomodoroSession?> {
        val result = safeApiCall { api.getActiveSession() }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    Resource.Success(response.body()?.data?.toDomain())
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading  -> Resource.Loading
        }
    }

    override suspend fun getSessionById(sessionId: String): Resource<PomodoroSession> {
        val result = safeApiCall {
            api.getSessionById("${ApiConstants.Pomodoro.SESSIONS}/$sessionId")
        }
        return mapResponse(result) { it.toDomain() }
    }

    override suspend fun getSessions(
        page: Int,
        size: Int,
        taskId: String?
    ): Resource<List<PomodoroSession>> {
        val result = safeApiCall { api.getSessions(page, size, taskId) }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val sessions = response.body()?.data?.content?.map { it.toDomain() } ?: emptyList()
                    Resource.Success(sessions)
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading  -> Resource.Loading
        }
    }

    override suspend fun getTodayStats(): Resource<PomodoroStats> {
        val result = safeApiCall { api.getTodayStats() }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val stats = response.body()?.data?.toDomain()
                        ?: return Resource.Error("Empty stats response.")
                    Resource.Success(stats)
                } else {
                    Resource.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            }
            is Resource.Error -> result
            Resource.Loading  -> Resource.Loading
        }
    }

    override fun observeSessions(): Flow<List<PomodoroSession>> = flow {
        val result = getSessions(page = 0, size = 50, taskId = null)
        if (result is Resource.Success) emit(result.data)
    }

    private fun <T, R> mapResponse(
        result: Resource<retrofit2.Response<com.oussama_chatri.productivityx.features.pomodoro.data.remote.ApiResponseWrapper<T>>>,
        transform: (T) -> R
    ): Resource<R> = when (result) {
        is Resource.Success -> {
            val response = result.data
            if (response.isSuccessful) {
                val data = response.body()?.data
                    ?: return Resource.Error("Empty response from server.")
                Resource.Success(transform(data))
            } else {
                Resource.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        }
        is Resource.Error -> result
        Resource.Loading  -> Resource.Loading
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Something went wrong."
        return try {
            Regex("\"message\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
                ?: "Something went wrong."
        } catch (e: Exception) {
            "Something went wrong."
        }
    }
}