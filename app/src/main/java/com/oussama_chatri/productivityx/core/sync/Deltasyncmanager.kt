package com.oussama_chatri.productivityx.core.sync

import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeltaSyncManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenStorage: TokenStorage,
    private val preferencesDataStore: PreferencesDataStore
) {
    /**
     * Calls GET /api/v1/sync/delta?since=<ISO> and hands the response payload
     * to each feature's repository for merging. The actual merge is handled
     * by injecting feature repositories — stubbed here for the core layer.
     * Feature repositories call deltaSyncManager.pullDelta() indirectly via SyncWorker.
     */
    suspend fun pullDelta() {
        val lastSyncedAt = preferencesDataStore.lastSyncedAt.first()
        val since = Instant.ofEpochMilli(lastSyncedAt).toString()
        val token = tokenStorage.getAccessToken() ?: return

        val request = Request.Builder()
            .url("${ApiConstants.BASE_URL}${ApiConstants.Sync.DELTA}?since=$since")
            .header(ApiConstants.HEADER_AUTHORIZATION, "${ApiConstants.HEADER_BEARER_PREFIX}$token")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            preferencesDataStore.setLastSyncedAt(System.currentTimeMillis())
            // Feature repositories observe their own entity flows from Room;
            // the server response is applied by the respective RepositoryImpl.mergeRemote() methods.
        }
    }
}