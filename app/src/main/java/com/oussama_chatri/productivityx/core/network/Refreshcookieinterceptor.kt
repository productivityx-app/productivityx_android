package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshCookieInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Inject stored refresh cookie on paths that need it
        val needsCookie = original.url.encodedPath.let {
            it.contains("/auth/refresh") || it.contains("/auth/logout")
        }

        val request = if (needsCookie) {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken != null) {
                original.newBuilder()
                    .header("Cookie", "refreshToken=$refreshToken")
                    .build()
            } else original
        } else original

        val response = chain.proceed(request)

        // Extract and persist refresh token from Set-Cookie header
        response.headers("Set-Cookie").forEach { cookieHeader ->
            if (cookieHeader.startsWith("refreshToken=")) {
                val rawValue = cookieHeader
                    .substringAfter("refreshToken=")
                    .substringBefore(";")
                    .trim()
                if (rawValue.isNotBlank()) {
                    tokenStorage.saveRefreshToken(rawValue)
                }
            }
        }

        return response
    }
}