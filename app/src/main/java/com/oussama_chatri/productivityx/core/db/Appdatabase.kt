package com.oussama_chatri.productivityx.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueEntity
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteTagCrossRef
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagEntity
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import com.oussama_chatri.productivityx.features.tasks.data.local.entity.TaskEntity

@Database(
    entities = [
        SyncQueueEntity::class,
        NoteEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        TaskEntity::class,
        EventEntity::class,
    ],
    version = 3,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "productivityx.db"
    }
}