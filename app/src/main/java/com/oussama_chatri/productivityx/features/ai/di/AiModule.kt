package com.oussama_chatri.productivityx.features.ai.di

import com.oussama_chatri.productivityx.BuildConfig
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.features.ai.data.remote.api.AiApiService
import com.oussama_chatri.productivityx.features.ai.data.repository.AiRepositoryImpl
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    companion object {

        @Provides
        @Singleton
        fun provideAiApiService(retrofit: Retrofit): AiApiService =
            retrofit.create(AiApiService::class.java)

        @Provides
        @Singleton
        @Named("base_url")
        fun provideBaseUrl(): String =
            if (BuildConfig.DEBUG) ApiConstants.BASE_URL_DEV else ApiConstants.BASE_URL

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient         = true
            encodeDefaults    = true
        }
    }
}