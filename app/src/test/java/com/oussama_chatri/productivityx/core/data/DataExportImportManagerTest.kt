package com.oussama_chatri.productivityx.core.data

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.File

class DataExportImportManagerTest {

    private lateinit var gson: Gson
    private lateinit var encryptionUtil: DataEncryptionUtil
    private lateinit var noteDao: NoteDao
    private lateinit var taskDao: TaskDao
    private lateinit var eventDao: EventDao
    private lateinit var tagDao: TagDao
    private lateinit var pomodoroSessionDao: PomodoroSessionDao
    private lateinit var prefs: PreferencesDataStore
    private lateinit var manager: DataExportImportManager

    @Before
    fun setup() {
        gson = mockk(relaxed = true)
        encryptionUtil = mockk()
        noteDao = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)
        eventDao = mockk(relaxed = true)
        tagDao = mockk(relaxed = true)
        pomodoroSessionDao = mockk(relaxed = true)
        prefs = mockk()

        coEvery { prefs.cachedUserId } returns flowOf("test-user")

        manager = DataExportImportManager(
            gson = gson,
            encryptionUtil = encryptionUtil,
            noteDao = noteDao,
            taskDao = taskDao,
            eventDao = eventDao,
            tagDao = tagDao,
            pomodoroSessionDao = pomodoroSessionDao,
            prefs = prefs
        )
    }

    @Test
    fun `exportToFile successfully exports data`() = runBlocking {
        // Arrange
        val file = File("test_export.px")
        
        coEvery { noteDao.searchNotes("test-user", "") } returns emptyList()
        coEvery { taskDao.getAllNonDeleted() } returns emptyList()
        coEvery { eventDao.getAllNonDeleted() } returns emptyList()
        coEvery { tagDao.getTags("test-user") } returns emptyList()
        coEvery { pomodoroSessionDao.getRecentSessions("test-user", Int.MAX_VALUE) } returns emptyList()
        
        every { gson.toJson(any()) } returns "{}"
        coEvery { encryptionUtil.encryptToFile(any(), file) } returns Unit

        // Act
        manager.exportToFile(file)

        // Assert
        coVerify(exactly = 1) { encryptionUtil.encryptToFile(any(), file) }
    }

    @Test
    fun `importFromFile successfully imports data`() = runBlocking {
        // Arrange
        val file = File("test_import.px")
        val jsonPayload = """{"notes":[],"tasks":[],"events":[],"tags":[],"pomodoroSessions":[]}"""
        val mockPayload = ExportPayload(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        
        coEvery { encryptionUtil.decryptFromFile(file.absolutePath) } returns jsonPayload
        every { gson.fromJson(jsonPayload, ExportPayload::class.java) } returns mockPayload

        // Act
        manager.importFromFile(file)

        // Assert
        coVerify(exactly = 1) { noteDao.upsertAll(emptyList()) }
        coVerify(exactly = 1) { taskDao.insertAll(emptyList()) }
        coVerify(exactly = 1) { eventDao.upsertAll(emptyList()) }
        coVerify(exactly = 1) { tagDao.upsertAll(emptyList()) }
        coVerify(exactly = 1) { pomodoroSessionDao.insertAll(emptyList()) }
    }
}
