package com.example.memos.data.api.dto

import com.example.memos.data.model.Attachment

data class AttachmentDto(
    val name: String? = null,
    val createTime: String? = null,
    val filename: String? = null,
    val type: String? = null,
    val size: Long? = null,
    val memo: String? = null,
    val externalLink: String? = null
)

fun AttachmentDto.toDomain(): Attachment = Attachment(
    name = name ?: "",
    filename = filename ?: "",
    type = type,
    size = size
)

data class ListAttachmentsResponseDto(
    val attachments: List<AttachmentDto>? = null,
    val nextPageToken: String? = null
)
