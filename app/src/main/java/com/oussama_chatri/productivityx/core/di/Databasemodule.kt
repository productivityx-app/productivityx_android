package com.oussama_chatri.productivityx.core.di

import android.content.Context
import androidx.room.Room
import com.oussama_chatri.productivityx.core.db.AppDatabase
import com.oussama_chatri.productivityx.core.db.migration.MIGRATION_3_4
import com.oussama_chatri.productivityx.core.db.migration.MIGRATION_4_5
import com.oussama_chatri.productivityx.core.db.migration.MIGRATION_5_6
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
import com.oussama_chatri.productivityx.features.ai.data.local.dao.ConversationDao
import com.oussama_chatri.productivityx.features.ai.data.local.dao.MessageDao
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.local.TagDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
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
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao             = db.syncQueueDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao                        = db.noteDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao                          = db.tagDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao                        = db.taskDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao                      = db.eventDao()
    @Provides fun provideConversationDao(db: AppDatabase): ConversationDao        = db.conversationDao()
    @Provides fun provideMessageDao(db: AppDatabase): MessageDao                  = db.messageDao()
    @Provides fun providePomodoroSessionDao(db: AppDatabase): PomodoroSessionDao  = db.pomodoroSessionDao()
}