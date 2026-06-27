package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncModeManager @Inject constructor(
    private val prefs: PreferencesDataStore
) {
    private var cached: Boolean = runBlocking { prefs.localOnlyMode.first() }

    fun isSyncEnabled(): Boolean = !cached

    suspend fun refresh() {
        cached = prefs.isLocalOnly()
    }
}
