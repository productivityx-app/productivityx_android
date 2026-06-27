package com.oussama_chatri.productivityx.core.data

import com.google.gson.Gson
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.entity.PomodoroSessionEntity
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

data class ExportPayload(
    val notes: List<NoteEntity>,
    val tasks: List<TaskEntity>,
    val events: List<EventEntity>,
    val tags: List<TagEntity>,
    val pomodoroSessions: List<PomodoroSessionEntity>
)

@ViewModelScoped
class DataExportImportManager @Inject constructor(
    private val gson: Gson,
    private val encryptionUtil: DataEncryptionUtil,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val eventDao: EventDao,
    private val tagDao: TagDao,
    private val pomodoroSessionDao: PomodoroSessionDao,
    private val prefs: PreferencesDataStore
) {
    private val userId: String by lazy {
        runBlocking { prefs.cachedUserId.first() ?: "local" }
    }

    suspend fun exportToFile(file: File) {
        val payload = ExportPayload(
            notes = noteDao.searchNotes(userId, ""),
            tasks = taskDao.getAllNonDeleted(),
            events = eventDao.getAllNonDeleted(),
            tags = tagDao.getTags(userId),
            pomodoroSessions = pomodoroSessionDao.getRecentSessions(userId, Int.MAX_VALUE)
        )
        val json = gson.toJson(payload)
        encryptionUtil.encryptToFile(json, file)
    }

    suspend fun importFromFile(file: File) {
        val json = encryptionUtil.decryptFromFile(file.absolutePath)
        val payload = gson.fromJson(json, ExportPayload::class.java)

        noteDao.upsertAll(payload.notes)
        taskDao.insertAll(payload.tasks)
        eventDao.upsertAll(payload.events)
        tagDao.upsertAll(payload.tags)
        pomodoroSessionDao.insertAll(payload.pomodoroSessions)
    }
}
