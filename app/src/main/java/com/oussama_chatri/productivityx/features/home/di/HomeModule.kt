package com.oussama_chatri.productivityx.features.home.di

import com.oussama_chatri.productivityx.features.home.data.repository.HomeRepositoryImpl
import com.oussama_chatri.productivityx.features.home.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}
