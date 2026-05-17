package com.example.memos.data.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider.getToken() ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }
}
