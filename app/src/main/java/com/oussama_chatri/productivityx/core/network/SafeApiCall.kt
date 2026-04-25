package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.util.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import java.io.IOException

@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: T? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("timestamp") val timestamp: String? = null
)

suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(call())
    } catch (e: HttpException) {
        val httpCode = e.code()
        val body = e.response()?.errorBody()?.string()
        val message = when (httpCode) {
            400 -> "Bad request. Please check your input."
            401 -> "Session expired. Please sign in again."
            403 -> "You don't have permission to perform this action."
            404 -> "Resource not found."
            409 -> "Conflict — this item already exists."
            422 -> "Validation failed. Please review your input."
            429 -> "Too many requests. Please wait and try again."
            500 -> "Server error. Please try again later."
            else -> body ?: "Unexpected error (HTTP $httpCode)"
        }
        // code kept as String? for consistency with AuthResult and parseError()
        Resource.Error(message = message, code = httpCode.toString())
    } catch (e: IOException) {
        Resource.Error(message = "No internet connection. Please check your network.")
    } catch (e: Exception) {
        Resource.Error(message = e.localizedMessage ?: "An unexpected error occurred.")
    }
}