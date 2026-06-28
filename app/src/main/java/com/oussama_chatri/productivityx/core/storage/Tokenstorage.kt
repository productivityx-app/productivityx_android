package com.oussama_chatri.productivityx.core.storage

import kotlinx.coroutines.flow.SharedFlow

interface TokenStorage {
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?

    // The refresh token arrives as an HttpOnly cookie from the backend.
    // We extract it from the Set-Cookie header in the OkHttp interceptor
    // and store it here so we can send it back on refresh / logout.
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?

    fun clearAll()

    fun notifySessionExpired()

    val sessionExpiredEvents: SharedFlow<Unit>
}