package com.oussama_chatri.productivityx.features.settings.di

import com.oussama_chatri.productivityx.features.settings.data.remote.api.PreferencesApi
import com.oussama_chatri.productivityx.features.settings.data.remote.api.ProfileApi
import com.oussama_chatri.productivityx.features.settings.data.repository.PreferencesRepositoryImpl
import com.oussama_chatri.productivityx.features.settings.data.repository.ProfileRepositoryImpl
import com.oussama_chatri.productivityx.features.settings.domain.repository.PreferencesRepository
import com.oussama_chatri.productivityx.features.settings.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    companion object {

        @Provides
        @Singleton
        fun provideProfileApi(retrofit: Retrofit): ProfileApi =
            retrofit.create(ProfileApi::class.java)

        @Provides
        @Singleton
        fun providePreferencesApi(retrofit: Retrofit): PreferencesApi =
            retrofit.create(PreferencesApi::class.java)
    }
}
