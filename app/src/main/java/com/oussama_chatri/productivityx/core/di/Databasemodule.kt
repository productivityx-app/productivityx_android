package com.oussama_chatri.productivityx.core.di

import android.content.Context
import androidx.room.Room
import com.oussama_chatri.productivityx.core.db.AppDatabase
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao               = db.noteDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao                 = db.tagDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao               = db.taskDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao             = db.eventDao()
}