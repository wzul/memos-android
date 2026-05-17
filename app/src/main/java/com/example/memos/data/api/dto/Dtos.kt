package com.example.memos.data.api.dto

data class MemoDto(
    val name: String? = null,
    val state: String? = null,
    val creator: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val content: String = "",
    val visibility: String? = null,
    val tags: List<String>? = null,
    val pinned: Boolean? = null,
    val snippet: String? = null
)

data class CreateMemoRequestDto(
    val content: String,
    val visibility: String = "PRIVATE",
    val pinned: Boolean = false,
    val tags: List<String> = emptyList()
)

data class UpdateMemoRequestDto(
    val content: String? = null,
    val visibility: String? = null,
    val pinned: Boolean? = null,
    val tags: List<String>? = null
)

data class ListMemosResponseDto(
    val memos: List<MemoDto>? = null,
    val nextPageToken: String? = null
)

fun MemoDto.toEntity(): com.example.memos.data.db.entity.MemoEntity {
    return com.example.memos.data.db.entity.MemoEntity(
        name = name ?: "",
        content = content,
        state = state ?: "NORMAL",
        creator = creator,
        createTime = createTime?.let { java.time.Instant.parse(it).toEpochMilli() },
        updateTime = updateTime?.let { java.time.Instant.parse(it).toEpochMilli() },
        visibility = visibility ?: "PRIVATE",
        tags = tags?.joinToString(",") ?: "",
        pinned = pinned ?: false,
        snippet = snippet
    )
}

data class SignInRequestDto(
    val username: String,
    val password: String
)

data class SignInResponseDto(
    val token: String? = null,
    val accessToken: String? = null
)

data class RefreshTokenResponseDto(
    val accessToken: String? = null,
    val expiresAt: String? = null
)

data class UserDto(
    val name: String? = null,
    val role: String? = null,
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val state: String? = null
)
