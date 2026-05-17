package com.example.memos.data.api

import com.example.memos.data.security.EncryptedTokenStore
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val encryptedTokenStore: EncryptedTokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val instanceUrl = encryptedTokenStore.getInstanceUrl() ?: return chain.proceed(request)

        val parsed = instanceUrl.toHttpUrlOrNull() ?: return chain.proceed(request)

        val newUrl = request.url.newBuilder()
            .scheme(parsed.scheme)
            .host(parsed.host)
            .port(parsed.port)
            .build()

        val newRequest = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
