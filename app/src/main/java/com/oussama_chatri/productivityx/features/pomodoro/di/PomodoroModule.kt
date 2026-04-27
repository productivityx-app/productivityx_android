    package com.oussama_chatri.productivityx.features.pomodoro.di

import com.oussama_chatri.productivityx.features.pomodoro.data.remote.PomodoroApi
import com.oussama_chatri.productivityx.features.pomodoro.data.repository.PomodoroRepositoryImpl
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PomodoroModule {

    @Binds
    @Singleton
    abstract fun bindPomodoroRepository(
        impl: PomodoroRepositoryImpl
    ): PomodoroRepository

    companion object {

        @Provides
        @Singleton
        fun providePomodoroApi(retrofit: Retrofit): PomodoroApi =
            retrofit.create(PomodoroApi::class.java)
    }
}
