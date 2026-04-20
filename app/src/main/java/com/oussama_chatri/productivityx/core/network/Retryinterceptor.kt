package com.oussama_chatri.productivityx.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < ApiConstants.MAX_RETRY_ATTEMPTS) {
            try {
                val response = chain.proceed(chain.request())
                if (response.isSuccessful || response.code !in RETRYABLE_CODES) return response
                response.close()
            } catch (e: IOException) {
                lastException = e
            }

            attempt++
            if (attempt < ApiConstants.MAX_RETRY_ATTEMPTS) {
                Thread.sleep(BACKOFF_DELAYS[attempt])
            }
        }

        throw lastException ?: IOException("Max retry attempts reached")
    }

    companion object {
        private val RETRYABLE_CODES = setOf(408, 429, 500, 502, 503, 504)
        private val BACKOFF_DELAYS = longArrayOf(0L, 1_000L, 2_000L)
    }
}