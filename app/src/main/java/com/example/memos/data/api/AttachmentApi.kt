package com.example.memos.data.api

import com.example.memos.data.api.dto.AttachmentDto
import com.example.memos.data.api.dto.ListAttachmentsResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AttachmentApi {

    @Multipart
    @POST("api/v1/attachments")
    suspend fun uploadAttachment(
        @Part file: MultipartBody.Part
    ): Response<AttachmentDto>

    @GET("api/v1/attachments")
    suspend fun listAttachments(
        @Query("pageSize") pageSize: Int? = null,
        @Query("pageToken") pageToken: String? = null
    ): Response<ListAttachmentsResponseDto>

    @GET("api/v1/{name}")
    suspend fun getAttachment(
        @Path("name", encoded = true) name: String
    ): Response<AttachmentDto>

    @DELETE("api/v1/{name}")
    suspend fun deleteAttachment(
        @Path("name", encoded = true) name: String
    ): Response<Unit>
}
