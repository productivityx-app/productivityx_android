package com.oussama_chatri.productivityx.core.di

import android.content.Context
import androidx.room.Room
import com.oussama_chatri.productivityx.core.db.AppDatabase
import com.oussama_chatri.productivityx.core.db.sync.SyncQueueDao
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

//    @Provides fun provideNoteDao(db: AppDatabase) = db.noteDao()
//    @Provides fun provideTagDao(db: AppDatabase) = db.tagDao()
//    @Provides fun provideTaskDao(db: AppDatabase) = db.taskDao()
//    @Provides fun provideEventDao(db: AppDatabase) = db.eventDao()
//    @Provides fun providePomodoroSessionDao(db: AppDatabase) = db.pomodoroSessionDao()
//    @Provides fun provideProfileDao(db: AppDatabase) = db.profileDao()
//    @Provides fun provideUserPreferencesDao(db: AppDatabase) = db.userPreferencesDao()
//    @Provides fun provideConversationDao(db: AppDatabase) = db.conversationDao()
//    @Provides fun provideMessageDao(db: AppDatabase) = db.messageDao()
}