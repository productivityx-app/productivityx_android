package com.oussama_chatri.productivityx.core.sync

import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.isSyncEnabled
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboxProcessor @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val okHttpClient: OkHttpClient,
    private val tokenStorage: TokenStorage,
    private val prefs: PreferencesDataStore
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    suspend fun drainOutbox() {
        if (!prefs.isSyncEnabled()) return
        val pending = syncQueueDao.getPending()
        for (item in pending) {
            val now = System.currentTimeMillis()
            syncQueueDao.updateStatus(item.id, SyncStatus.SYNCING, now)

            val success = try {
                dispatchToApi(
                    entityType = item.entityType,
                    entityId = item.entityId,
                    operation = item.operation,
                    payload = item.payload
                )
            } catch (e: Exception) {
                false
            }

            if (success) {
                syncQueueDao.delete(item.id)
            } else {
                val backoff = minOf(
                    (Math.pow(2.0, item.retryCount.toDouble()) * 1000L).toLong(),
                    30_000L
                )
                syncQueueDao.updateStatus(item.id, SyncStatus.PENDING, now + backoff)
            }
        }
    }

    private fun dispatchToApi(
        entityType: EntityType,
        entityId: String,
        operation: SyncOperation,
        payload: String
    ): Boolean {
        val token = tokenStorage.getAccessToken() ?: return false

        val isPomodoroUpdate = entityType == EntityType.POMODORO && operation == SyncOperation.UPDATE
        val url = if (isPomodoroUpdate) {
            resolvePomodoroUpdateUrl(entityId, payload)
        } else {
            resolveUrl(entityType, entityId, operation)
        } ?: return false

        val requestBuilder = Request.Builder()
            .url("${ApiConstants.BASE_URL}$url")
            .header(ApiConstants.HEADER_AUTHORIZATION, "${ApiConstants.HEADER_BEARER_PREFIX}$token")

        val request = when {
            isPomodoroUpdate ->
                requestBuilder.patch(RequestBody.create(jsonMediaType, payload)).build()
            operation == SyncOperation.CREATE ->
                requestBuilder.post(RequestBody.create(jsonMediaType, payload)).build()
            operation == SyncOperation.UPDATE ->
                requestBuilder.put(RequestBody.create(jsonMediaType, payload)).build()
            else ->
                requestBuilder.delete().build()
        }

        val response = okHttpClient.newCall(request).execute()
        return response.isSuccessful
    }

    private fun resolveUrl(type: EntityType, id: String, op: SyncOperation): String? = when (type) {
        EntityType.NOTE -> when (op) {
            SyncOperation.CREATE -> ApiConstants.Notes.BASE
            SyncOperation.UPDATE, SyncOperation.DELETE -> ApiConstants.Notes.byId(id)
        }
        EntityType.TASK -> when (op) {
            SyncOperation.CREATE -> ApiConstants.Tasks.BASE
            SyncOperation.UPDATE, SyncOperation.DELETE -> ApiConstants.Tasks.byId(id)
        }
        EntityType.EVENT -> when (op) {
            SyncOperation.CREATE -> ApiConstants.Events.BASE
            SyncOperation.UPDATE, SyncOperation.DELETE -> ApiConstants.Events.byId(id)
        }
        EntityType.PROFILE -> when (op) {
            SyncOperation.UPDATE -> ApiConstants.Profile.BASE
            else -> null
        }
        EntityType.PREFERENCES -> when (op) {
            SyncOperation.UPDATE -> ApiConstants.Preferences.BASE
            else -> null
        }
        EntityType.POMODORO -> when (op) {
            SyncOperation.CREATE -> ApiConstants.Pomodoro.START
            else -> null
        }
    }

    private fun resolvePomodoroUpdateUrl(sessionId: String, payload: String): String? {
        return try {
            val json = JSONObject(payload)
            when (json.optString("action")) {
                "end" -> ApiConstants.Pomodoro.end(sessionId)
                "interrupt" -> ApiConstants.Pomodoro.interrupt(sessionId)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}