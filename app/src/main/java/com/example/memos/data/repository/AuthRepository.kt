package com.example.memos.data.repository

import com.example.memos.data.model.User

interface AuthRepository {
    suspend fun signIn(instanceUrl: String, accessToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    fun isLoggedIn(): Boolean
}
