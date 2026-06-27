package com.oussama_chatri.productivityx.core.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.oussama_chatri.productivityx.core.util.Resource
import retrofit2.HttpException
import java.io.IOException

data class ApiResponse<T>(
    @SerializedName("success")   val success: Boolean,
    @SerializedName("data")      val data: T? = null,
    @SerializedName("message")   val message: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

private val gson = Gson()

suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(call())
    } catch (e: HttpException) {
        val httpCode = e.code()
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let {
            runCatching { gson.fromJson(it, ApiResponse::class.java) }.getOrNull()
        }
        val message = parsed?.message ?: when (httpCode) {
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
        Resource.Error(message = message, code = parsed?.errorCode ?: httpCode.toString())
    } catch (e: IOException) {
        Resource.Error(message = "No internet connection. Please check your network.")
    } catch (e: Exception) {
        Resource.Error(message = e.localizedMessage ?: "An unexpected error occurred.")
    }
}
