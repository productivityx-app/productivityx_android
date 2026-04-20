package com.oussama_chatri.productivityx.core.storage

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenStorageImpl @Inject constructor(
    @Named("encrypted") private val prefs: SharedPreferences
) : TokenStorage {

    override fun saveAccessToken(token: String) =
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()

    override fun getAccessToken(): String? =
        prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun saveRefreshToken(token: String) =
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()

    override fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun clearAll() =
        prefs.edit().clear().apply()

    companion object {
        private const val KEY_ACCESS_TOKEN  = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}