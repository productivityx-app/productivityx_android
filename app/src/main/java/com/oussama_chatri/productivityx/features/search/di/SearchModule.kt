package com.oussama_chatri.productivityx.features.search.di

import com.oussama_chatri.productivityx.features.search.data.remote.api.SearchApiService
import com.oussama_chatri.productivityx.features.search.data.repository.SearchRepositoryImpl
import com.oussama_chatri.productivityx.features.search.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository

    companion object {
        @Provides
        @Singleton
        fun provideSearchApiService(retrofit: Retrofit): SearchApiService =
            retrofit.create(SearchApiService::class.java)
    }
}
