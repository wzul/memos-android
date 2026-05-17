package com.example.memos.data.api

import com.example.memos.data.security.EncryptedTokenStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(
    private val encryptedTokenStore: EncryptedTokenStore
) {
    fun getToken(): String? = encryptedTokenStore.getAccessToken()
}
