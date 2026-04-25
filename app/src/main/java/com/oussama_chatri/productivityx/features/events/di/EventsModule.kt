package com.oussama_chatri.productivityx.features.events.di

import com.google.gson.Gson
import com.oussama_chatri.productivityx.features.events.data.remote.EventApi
import com.oussama_chatri.productivityx.features.events.data.repository.EventRepositoryImpl
import com.oussama_chatri.productivityx.features.events.domain.repository.EventRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EventsModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    companion object {

        @Provides
        @Singleton
        fun provideEventApi(retrofit: Retrofit): EventApi =
            retrofit.create(EventApi::class.java)
    }
}
