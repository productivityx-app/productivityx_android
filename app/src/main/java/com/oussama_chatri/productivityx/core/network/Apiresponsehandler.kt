package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> = withContext(Dispatchers.IO) {
    try {
        Resource.Success(call())
    } catch (e: HttpException) {
        val code = e.code()
        val message = when (code) {
            401 -> "AUTH_TOKEN_INVALID"
            403 -> "GEN_FORBIDDEN"
            404 -> "RES_NOT_FOUND"
            409 -> "CONFLICT"
            429 -> "RATE_LIMITED"
            in 500..599 -> "GEN_INTERNAL_ERROR"
            else -> "HTTP_$code"
        }
        Resource.Error(e.message(), message)
    } catch (e: SocketTimeoutException) {
        Resource.Error("Request timed out", "TIMEOUT")
    } catch (e: IOException) {
        Resource.Error("No network connection", "NETWORK")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error", "UNKNOWN")
    }
}