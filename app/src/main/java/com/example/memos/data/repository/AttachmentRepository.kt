package com.example.memos.data.repository

import android.content.Context
import android.net.Uri
import com.example.memos.data.api.AttachmentApi
import com.example.memos.data.api.dto.AttachmentDto
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepository @Inject constructor(
    private val api: AttachmentApi,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadAttachment(uri: Uri): Result<AttachmentDto> = runCatching {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = api.uploadAttachment(body)
        if (!response.isSuccessful) throw IllegalStateException("Upload failed: ${response.code()}")
        response.body() ?: throw IllegalStateException("Empty response")
    }

    suspend fun deleteAttachment(name: String): Result<Unit> = runCatching {
        val response = api.deleteAttachment(name)
        if (!response.isSuccessful) throw IllegalStateException("Delete failed: ${response.code()}")
    }

    private fun uriToFile(uri: Uri): File {
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
