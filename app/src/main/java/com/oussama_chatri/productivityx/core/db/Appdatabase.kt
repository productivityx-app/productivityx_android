package com.oussama_chatri.productivityx.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
//import com.oussama_chatri.productivityx.features.ai.data.local.ConversationDao
//import com.oussama_chatri.productivityx.features.ai.data.local.ConversationEntity
//import com.oussama_chatri.productivityx.features.ai.data.local.MessageDao
//import com.oussama_chatri.productivityx.features.ai.data.local.MessageEntity
//import com.oussama_chatri.productivityx.features.events.data.local.EventDao
//import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
//import com.oussama_chatri.productivityx.features.notes.data.local.NoteFtsEntity
//import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
//import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
//import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
//import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
//import com.oussama_chatri.productivityx.features.pomodoro.data.local.PomodoroSessionDao
//import com.oussama_chatri.productivityx.features.pomodoro.data.local.PomodoroSessionEntity
//import com.oussama_chatri.productivityx.features.profile.data.local.ProfileDao
//import com.oussama_chatri.productivityx.features.profile.data.local.ProfileEntity
//import com.oussama_chatri.productivityx.features.profile.data.local.UserPreferencesDao
//import com.oussama_chatri.productivityx.features.profile.data.local.UserPreferencesEntity
//import com.oussama_chatri.productivityx.features.tasks.data.local.TaskDao
//import com.oussama_chatri.productivityx.features.tasks.data.local.TaskEntity
//import com.oussama_chatri.productivityx.features.tasks.data.local.TaskFtsEntity

@Database(
    entities = [
        SyncQueueEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun syncQueueDao(): SyncQueueDao
//    abstract fun noteDao(): NoteDao
//    abstract fun tagDao(): TagDao
//    abstract fun taskDao(): TaskDao
//    abstract fun eventDao(): EventDao
//    abstract fun pomodoroSessionDao(): PomodoroSessionDao
//    abstract fun profileDao(): ProfileDao
//    abstract fun userPreferencesDao(): UserPreferencesDao
//    abstract fun conversationDao(): ConversationDao
//    abstract fun messageDao(): MessageDao

    companion object {
        const val DATABASE_NAME = "productivityx.db"
    }
}