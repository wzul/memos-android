package com.example.memos.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.memos.data.api.AttachmentApi
import com.example.memos.data.api.AuthInterceptor
import com.example.memos.data.api.LocalDataSource
import com.example.memos.data.api.MemosApi
import com.example.memos.data.api.TokenProvider
import com.example.memos.data.security.EncryptedTokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLocalDataSource(dataStore: DataStore<Preferences>): LocalDataSource {
        return LocalDataSource(dataStore)
    }

    @Provides
    @Singleton
    fun provideTokenProvider(localDataSource: LocalDataSource): TokenProvider {
        return TokenProvider(localDataSource)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): AuthInterceptor {
        return AuthInterceptor(tokenProvider)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        localDataSource: LocalDataSource
    ): Retrofit {
        val baseUrl = runBlocking {
            val url = localDataSource.instanceUrl.first()
            (url?.removeSuffix("/") ?: "https://demo.usememos.com") + "/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMemosApi(retrofit: Retrofit): MemosApi {
        return retrofit.create(MemosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAttachmentApi(retrofit: Retrofit): AttachmentApi {
        return retrofit.create(AttachmentApi::class.java)
    }
}
