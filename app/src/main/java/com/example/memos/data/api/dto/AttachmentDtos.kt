package com.example.memos.data.api.dto

data class AttachmentDto(
    val name: String? = null,
    val createTime: String? = null,
    val filename: String? = null,
    val type: String? = null,
    val size: Long? = null,
    val memo: String? = null,
    val externalLink: String? = null
)

data class ListAttachmentsResponseDto(
    val attachments: List<AttachmentDto>? = null,
    val nextPageToken: String? = null
)
