package com.oussama_chatri.productivityx.features.notes.di

import com.google.gson.Gson
import com.oussama_chatri.productivityx.features.notes.data.remote.NoteApi
import com.oussama_chatri.productivityx.features.notes.data.remote.TagApi
import com.oussama_chatri.productivityx.features.notes.data.repository.NoteRepositoryImpl
import com.oussama_chatri.productivityx.features.notes.data.repository.TagRepositoryImpl
import com.oussama_chatri.productivityx.features.notes.domain.repository.NoteRepository
import com.oussama_chatri.productivityx.features.notes.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// DAOs (NoteDao, TagDao) are provided by DatabaseModule — no duplication here.
@Module
@InstallIn(SingletonComponent::class)
abstract class NotesModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    companion object {

        @Provides
        @Singleton
        fun provideNoteApi(retrofit: Retrofit): NoteApi = retrofit.create(NoteApi::class.java)

        @Provides
        @Singleton
        fun provideTagApi(retrofit: Retrofit): TagApi = retrofit.create(TagApi::class.java)

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()
    }
}