package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Provider

class TokenRefreshInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
    // Provider<> breaks the circular dependency: Authenticator → OkHttp → Authenticator
    private val okHttpClientProvider: Provider<okhttp3.OkHttpClient>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loops
        if (response.request.header(ApiConstants.HEADER_AUTHORIZATION) == null) return null
        if (responseCount(response) >= 2) return null

        val newToken = runBlocking { refreshAccessToken() } ?: return null

        return response.request.newBuilder()
            .header(ApiConstants.HEADER_AUTHORIZATION, "${ApiConstants.HEADER_BEARER_PREFIX}$newToken")
            .build()
    }

    private fun refreshAccessToken(): String? {
        return try {
            val refreshToken = tokenStorage.getRefreshToken() ?: return null

            val requestBody = okhttp3.RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                """{"refreshToken":"$refreshToken"}"""
            )
            val request = okhttp3.Request.Builder()
                .url("${ApiConstants.BASE_URL}${ApiConstants.Auth.REFRESH}")
                .post(requestBody)
                .build()

            val resp = okHttpClientProvider.get().newCall(request).execute()
            if (!resp.isSuccessful) {
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