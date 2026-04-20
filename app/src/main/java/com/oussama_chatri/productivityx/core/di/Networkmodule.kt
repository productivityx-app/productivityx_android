package com.oussama_chatri.productivityx.core.di

import com.oussama_chatri.productivityx.BuildConfig
import com.oussama_chatri.productivityx.core.network.ApiConstants
import com.oussama_chatri.productivityx.core.network.AuthInterceptor
import com.oussama_chatri.productivityx.core.network.RefreshCookieInterceptor
import com.oussama_chatri.productivityx.core.network.RetryInterceptor
import com.oussama_chatri.productivityx.core.network.SseClient
import com.oussama_chatri.productivityx.core.network.TokenRefreshInterceptor
import com.oussama_chatri.productivityx.core.storage.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStorage: TokenStorage): AuthInterceptor =
        AuthInterceptor(tokenStorage)

    @Provides
    @Singleton
    fun provideRefreshCookieInterceptor(tokenStorage: TokenStorage): RefreshCookieInterceptor =
        RefreshCookieInterceptor(tokenStorage)

    @Provides
    @Singleton
    fun provideRetryInterceptor(): RetryInterceptor = RetryInterceptor()

    @Provides
    @Singleton
    fun provideTokenRefreshInterceptor(
        tokenStorage: TokenStorage,
        okHttpClientProvider: Provider<OkHttpClient>
    ): TokenRefreshInterceptor = TokenRefreshInterceptor(tokenStorage, okHttpClientProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        refreshCookieInterceptor: RefreshCookieInterceptor,
        retryInterceptor: RetryInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConstants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(ApiConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(ApiConstants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
        .addInterceptor(retryInterceptor)
        .addInterceptor(refreshCookieInterceptor)   // extracts/injects refresh cookie
        .addInterceptor(authInterceptor)             // injects Bearer access token
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenRefreshInterceptor)      // handles 401 → refresh → retry
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(
            if (BuildConfig.DEBUG) ApiConstants.BASE_URL_DEV else ApiConstants.BASE_URL
        )
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideSseClient(okHttpClient: OkHttpClient): SseClient = SseClient(okHttpClient)
}