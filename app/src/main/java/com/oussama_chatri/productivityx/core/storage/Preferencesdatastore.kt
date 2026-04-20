package com.oussama_chatri.productivityx.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "px_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    val onboardingSeen: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING_SEEN] ?: false }

    val appTheme: Flow<String> = store.data.map { it[KEY_THEME] ?: "DARK" }

    val lastSyncedAt: Flow<Long> = store.data.map { it[KEY_LAST_SYNCED_AT] ?: 0L }

    val cachedUserId: Flow<String?> = store.data.map { it[KEY_USER_ID] }

    val cachedUserFirstName: Flow<String?> = store.data.map { it[KEY_USER_FIRST_NAME] }

    val cachedUserEmail: Flow<String?> = store.data.map { it[KEY_USER_EMAIL] }

    suspend fun setOnboardingSeen(seen: Boolean) {
        store.edit { it[KEY_ONBOARDING_SEEN] = seen }
    }

    suspend fun setTheme(theme: String) {
        store.edit { it[KEY_THEME] = theme }
    }

    suspend fun setLastSyncedAt(epochMs: Long) {
        store.edit { it[KEY_LAST_SYNCED_AT] = epochMs }
    }

    suspend fun cacheUser(id: String, firstName: String, email: String) {
        store.edit {
            it[KEY_USER_ID] = id
            it[KEY_USER_FIRST_NAME] = firstName
            it[KEY_USER_EMAIL] = email
        }
    }

    suspend fun clearUser() {
        store.edit {
            it.remove(KEY_USER_ID)
            it.remove(KEY_USER_FIRST_NAME)
            it.remove(KEY_USER_EMAIL)
            it.remove(KEY_LAST_SYNCED_AT)
        }
    }

    companion object {
        private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }
}