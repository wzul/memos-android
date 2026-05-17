package com.example.memos.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Memo(
    val name: String,
    val content: String,
    val state: State = State.NORMAL,
    val visibility: Visibility = Visibility.PRIVATE,
    val pinned: Boolean = false,
    val tags: List<String> = emptyList(),
    val creator: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val snippet: String? = null
) : Parcelable

enum class State { NORMAL, ARCHIVED }

enum class Visibility { PRIVATE, PROTECTED, PUBLIC }

data class User(
    val name: String,
    val username: String,
    val role: Role = Role.USER,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val state: State = State.NORMAL
)

enum class Role { ADMIN, USER }

data class AuthTokens(
    val accessToken: String,
    val expiresAt: String? = null
)
