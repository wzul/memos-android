package com.example.memos.data.api

import com.example.memos.data.api.dto.CreateMemoRequestDto
import com.example.memos.data.api.dto.ListAttachmentsResponseDto
import com.example.memos.data.api.dto.ListMemosResponseDto
import com.example.memos.data.api.dto.MemoDto
import com.example.memos.data.api.dto.RefreshTokenResponseDto
import com.example.memos.data.api.dto.SignInRequestDto
import com.example.memos.data.api.dto.SignInResponseDto
import com.example.memos.data.api.dto.UpdateMemoRequestDto
import com.example.memos.data.api.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MemosApi {

    // Auth
    @POST("api/v1/auth/signin")
    suspend fun signIn(@Body request: SignInRequestDto): Response<SignInResponseDto>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @POST("api/v1/auth/signout")
    suspend fun signOut(): Response<Unit>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(): Response<RefreshTokenResponseDto>

    // Memos
    @POST("api/v1/memos")
    suspend fun createMemo(@Body request: CreateMemoRequestDto): Response<MemoDto>

    @GET("api/v1/memos")
    suspend fun listMemos(
        @Query("pageSize") pageSize: Int? = null,
        @Query("pageToken") pageToken: String? = null,
        @Query("state") state: String? = null,
        @Query("filter") filter: String? = null
    ): Response<ListMemosResponseDto>

    @GET("api/v1/{name}")
    suspend fun getMemo(@Path("name", encoded = true) name: String): Response<MemoDto>

    @PATCH("api/v1/{name}")
    suspend fun updateMemo(
        @Path("name", encoded = true) name: String,
        @Query("updateMask") updateMask: String,
        @Body request: UpdateMemoRequestDto
    ): Response<MemoDto>

    @DELETE("api/v1/{name}")
    suspend fun deleteMemo(@Path("name", encoded = true) name: String): Response<Unit>

    @GET("api/v1/{name}/attachments")
    suspend fun listMemoAttachments(@Path("name", encoded = true) name: String): Response<ListAttachmentsResponseDto>
}
