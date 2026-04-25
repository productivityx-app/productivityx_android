package com.oussama_chatri.productivityx.features.tasks.di

import com.oussama_chatri.productivityx.features.tasks.data.remote.api.TaskApi
import com.oussama_chatri.productivityx.features.tasks.data.repository.TaskRepositoryImpl
import com.oussama_chatri.productivityx.features.tasks.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}

@Module
@InstallIn(SingletonComponent::class)
object TaskNetworkModule {

    @Provides
    @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)
}
