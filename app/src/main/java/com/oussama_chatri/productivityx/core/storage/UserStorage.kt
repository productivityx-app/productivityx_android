package com.oussama_chatri.productivityx.core.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.UserResponse
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserStorage @Inject constructor(
    @Named("encrypted") private val prefs: SharedPreferences
) {
    private val gson = Gson()

    fun save(user: UserResponse) =
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()

    fun get(): UserResponse? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(json, UserResponse::class.java) }.getOrNull()
    }

    fun clear() = prefs.edit().remove(KEY_USER).apply()

    companion object {
        private const val KEY_USER = "cached_user"
    }
}
