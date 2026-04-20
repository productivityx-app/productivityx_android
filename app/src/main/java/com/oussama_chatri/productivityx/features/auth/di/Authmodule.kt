package com.oussama_chatri.productivityx.features.auth.di

import android.content.SharedPreferences
import com.oussama_chatri.productivityx.core.storage.UserStorage
import com.oussama_chatri.productivityx.features.auth.data.remote.AuthApi
import com.oussama_chatri.productivityx.features.auth.data.repository.AuthRepositoryImpl
import com.oussama_chatri.productivityx.features.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi =
            retrofit.create(AuthApi::class.java)

        @Provides
        @Singleton
        fun provideUserStorage(@Named("encrypted") prefs: SharedPreferences): UserStorage =
            UserStorage(prefs)
    }
}