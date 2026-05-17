package com.example.memos.data.repository

import com.example.memos.data.api.LocalDataSource
import com.example.memos.data.api.MemosApi
import com.example.memos.data.api.dto.UserDto
import com.example.memos.data.model.Role
import com.example.memos.data.model.State
import com.example.memos.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: MemosApi,
    private val localDataSource: LocalDataSource
) : AuthRepository {

    private val gson = Gson()

    override suspend fun signIn(instanceUrl: String, accessToken: String): Result<User> = runCatching {
        localDataSource.saveCredentials(instanceUrl, accessToken)
        val response = api.getCurrentUser()
        if (!response.isSuccessful) {
            localDataSource.clearCredentials()
            throw IllegalStateException("Invalid token or server error: ${response.code()}")
        }
        val userDto = response.body()
            ?: throw IllegalStateException("Empty response from server")
        val user = userDto.toDomain()
        localDataSource.saveCurrentUser(gson.toJson(userDto))
        user
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        try { api.signOut() } catch (_: Exception) { }
        localDataSource.clearCredentials()
    }

    override suspend fun getCurrentUser(): Result<User> = runCatching {
        val token = localDataSource.accessToken.firstOrNull()
            ?: throw IllegalStateException("Not logged in")
        val response = api.getCurrentUser()
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to get user: ${response.code()}")
        }
        val userDto = response.body()
            ?: throw IllegalStateException("Empty response")
        val user = userDto.toDomain()
        localDataSource.saveCurrentUser(gson.toJson(userDto))
        user
    }

    override fun isLoggedIn(): Boolean {
        // This is synchronous for simplicity; in production use Flow
        return runCatching {
            kotlinx.coroutines.runBlocking {
                localDataSource.accessToken.first() != null
            }
        }.getOrDefault(false)
    }

    private fun UserDto.toDomain(): User = User(
        name = name ?: "",
        username = username ?: "",
        role = role?.let { Role.valueOf(it) } ?: Role.USER,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        state = state?.let { State.valueOf(it) } ?: State.NORMAL
    )
}
