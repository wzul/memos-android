package com.example.memos.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.memos.data.api.AttachmentApi
import com.example.memos.data.api.AuthInterceptor
import com.example.memos.data.api.DynamicBaseUrlInterceptor
import com.example.memos.data.api.LocalDataSource
import com.example.memos.data.api.MemosApi
import com.example.memos.data.api.TokenProvider
import com.example.memos.data.security.EncryptedTokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideEncryptedTokenStore(@ApplicationContext context: Context): EncryptedTokenStore {
        return EncryptedTokenStore(context)
    }

    @Provides
    @Singleton
    fun provideTokenProvider(encryptedTokenStore: EncryptedTokenStore): TokenProvider {
        return TokenProvider(encryptedTokenStore)
    }

    @Provides
    @Singleton
    fun provideDynamicBaseUrlInterceptor(encryptedTokenStore: EncryptedTokenStore): DynamicBaseUrlInterceptor {
        return DynamicBaseUrlInterceptor(encryptedTokenStore)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): AuthInterceptor {
        return AuthInterceptor(tokenProvider)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://localhost/")
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
