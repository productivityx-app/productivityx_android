package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.BuildConfig
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TokenRefreshInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val refreshCookieInterceptor: RefreshCookieInterceptor,
) : Authenticator {

    private val mutex = Mutex()
    private var inFlightRefresh: CompletableDeferred<String?>? = null

    private val refreshClient: okhttp3.OkHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .addInterceptor(refreshCookieInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(ApiConstants.HEADER_AUTHORIZATION) == null) return null
        if (responseCount(response) >= 2) return null

        val newToken = runBlocking {
            mutex.withLock {
                val existing = inFlightRefresh
                if (existing != null) {
                    return@withLock existing.await()
                }
                val deferred = CompletableDeferred<String?>()
                inFlightRefresh = deferred
                val result = refreshAccessToken()
                deferred.complete(result)
                inFlightRefresh = null
                result
            }
        } ?: return null

        return response.request.newBuilder()
            .header(ApiConstants.HEADER_AUTHORIZATION, "${ApiConstants.HEADER_BEARER_PREFIX}$newToken")
            .build()
    }

    private fun refreshAccessToken(): String? {
        return try {
            if (tokenStorage.getRefreshToken() == null) return null

            val request = Request.Builder()
                .url("${ApiConstants.BASE_URL}${ApiConstants.Auth.REFRESH}")
                .post("".toRequestBody())
                .build()

            val resp = refreshClient.newCall(request).execute()
            if (!resp.isSuccessful) {
                tokenStorage.notifySessionExpired()
                tokenStorage.clearAll()
                return null
            }

            val body = resp.body?.string() ?: return null
            val json = JSONObject(body)
            val data = json.optJSONObject("data") ?: return null
            val newAccess = data.optString("accessToken").takeIf { it.isNotBlank() } ?: return null

            tokenStorage.saveAccessToken(newAccess)
            newAccess
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
