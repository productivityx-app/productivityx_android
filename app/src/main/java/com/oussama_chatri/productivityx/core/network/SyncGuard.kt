package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore

suspend fun PreferencesDataStore.isSyncEnabled(): Boolean = !isLocalOnly() && !isOfflineMode()
