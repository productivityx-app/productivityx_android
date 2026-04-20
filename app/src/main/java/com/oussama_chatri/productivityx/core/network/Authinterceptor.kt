package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStorage.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header(
                    ApiConstants.HEADER_AUTHORIZATION,
                    "${ApiConstants.HEADER_BEARER_PREFIX}$token"
                )
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}