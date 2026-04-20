package com.oussama_chatri.productivityx.features.auth.data.local

import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import com.oussama_chatri.productivityx.core.storage.UserStorage
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.UserResponse
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthToken
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthUser
import javax.inject.Inject

class AuthLocalDataSource @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val userStorage: UserStorage,
    private val preferencesDataStore: PreferencesDataStore
) {

    // Token ops

    fun saveToken(token: AuthToken) {
        tokenStorage.saveAccessToken(token.accessToken)
    }

    fun getAccessToken(): String? = tokenStorage.getAccessToken()

    fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null

    fun clearTokens() {
        tokenStorage.clearAll()
    }

    // User ops

    /** Persists the full UserResponse to encrypted storage. */
    fun saveUserResponse(userResponse: UserResponse) {
        userStorage.save(userResponse)
    }

    /** Returns the locally-cached UserResponse, or null if not yet fetched. */
    fun getCachedUserResponse(): UserResponse? = userStorage.get()

    suspend fun cacheUserForDataStore(user: AuthUser) {
        preferencesDataStore.cacheUser(
            id = user.id,
            firstName = user.firstName,
            email = user.email
        )
    }

    suspend fun clearUser() {
        userStorage.clear()
        preferencesDataStore.clearUser()
    }

    // Full clear (logout)

    suspend fun clearAll() {
        clearTokens()
        clearUser()
    }
}