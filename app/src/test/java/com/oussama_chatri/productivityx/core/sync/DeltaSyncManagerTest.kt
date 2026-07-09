package com.oussama_chatri.productivityx.core.sync

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

class DeltaSyncManagerTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var tokenStorage: TokenStorage
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var noteDao: NoteDao
    private lateinit var taskDao: TaskDao
    private lateinit var eventDao: EventDao
    private lateinit var pomodoroDao: PomodoroSessionDao
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var gson: Gson
    private lateinit var deltaSyncManager: DeltaSyncManager

    @Before
    fun setup() {
        okHttpClient = mockk()
        tokenStorage = mockk()
        preferencesDataStore = mockk()
        noteDao = mockk()
        taskDao = mockk()
        eventDao = mockk()
        pomodoroDao = mockk()
        syncQueueDao = mockk()
        conflictResolver = mockk()
        gson = Gson()

        deltaSyncManager = DeltaSyncManager(
            okHttpClient = okHttpClient,
            tokenStorage = tokenStorage,
            preferencesDataStore = preferencesDataStore,
            noteDao = noteDao,
            taskDao = taskDao,
            eventDao = eventDao,
            pomodoroDao = pomodoroDao,
            syncQueueDao = syncQueueDao,
            conflictResolver = conflictResolver,
            gson = gson
        )
    }

    @Test
    fun `pullDelta skips sync if disabled in preferences`() = runBlocking {
        // Arrange
        coEvery { preferencesDataStore.isLocalOnly() } returns true
        coEvery { preferencesDataStore.isOfflineMode() } returns false

        // Act
        deltaSyncManager.pullDelta()

        // Assert
        // tokenStorage should not be called if sync is disabled
        io.mockk.verify(exactly = 0) { tokenStorage.getAccessToken() }
    }

    @Test
    fun `pullDelta successfully processes empty delta`() = runBlocking {
        // Arrange
        coEvery { preferencesDataStore.isLocalOnly() } returns false
        coEvery { preferencesDataStore.isOfflineMode() } returns false
        coEvery { tokenStorage.getAccessToken() } returns "fake-token"
        coEvery { preferencesDataStore.cachedUserId } returns flowOf("user-123")
        coEvery { preferencesDataStore.lastSyncedAt } returns flowOf(1000L)
        coEvery { preferencesDataStore.setLastSyncedAt(any()) } returns Unit

        val mockCall = mockk<Call>()
        val jsonResponse = """{"success":true,"data":{"hasMore":false,"notes":[],"tasks":[],"events":[],"pomodoroSessions":[]}}"""
        val mockResponse = Response.Builder()
            .request(Request.Builder().url("https://api.productivityx.com/sync/delta?since=1970-01-01T00:00:01Z").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(jsonResponse.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
        
        every { mockCall.execute() } returns mockResponse
        every { okHttpClient.newCall(any()) } returns mockCall

        // Act
        deltaSyncManager.pullDelta()

        // Assert
        io.mockk.verify(exactly = 1) { okHttpClient.newCall(any()) }
    }
}
