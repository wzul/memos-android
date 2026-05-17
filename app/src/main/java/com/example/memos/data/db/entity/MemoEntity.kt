package com.example.memos.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.memos.data.model.Attachment
import com.example.memos.data.model.Memo
import com.example.memos.data.model.State
import com.example.memos.data.model.Visibility
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey val name: String,
    val localId: String? = null,
    val content: String,
    val state: String = "NORMAL",
    val creator: String? = null,
    val createTime: Long? = null,
    val updateTime: Long? = null,
    val visibility: String = "PRIVATE",
    val tags: String = "",
    val pinned: Boolean = false,
    val snippet: String? = null,
    val attachmentsJson: String = "",
    val isDeleted: Boolean = false,
    val syncStatus: String = "SYNCED",
    val lastModified: Long = System.currentTimeMillis()
)

private val gson = Gson()
private val attachmentListType = object : TypeToken<List<Attachment>>() {}.type

fun MemoEntity.toDomain(): Memo = Memo(
    name = name,
    content = content,
    state = State.valueOf(state),
    visibility = Visibility.valueOf(visibility),
    pinned = pinned,
    tags = if (tags.isBlank()) emptyList() else tags.split(","),
    creator = creator,
    createTime = createTime?.let { java.time.Instant.ofEpochMilli(it).toString() },
    updateTime = updateTime?.let { java.time.Instant.ofEpochMilli(it).toString() },
    snippet = snippet,
    attachments = if (attachmentsJson.isBlank()) emptyList() else gson.fromJson(attachmentsJson, attachmentListType)
)

fun Memo.toEntity(syncStatus: String = "SYNCED"): MemoEntity = MemoEntity(
    name = name,
    content = content,
    state = state.name,
    visibility = visibility.name,
    pinned = pinned,
    tags = tags.joinToString(","),
    creator = creator,
    createTime = createTime?.let { java.time.Instant.parse(it).toEpochMilli() },
    updateTime = updateTime?.let { java.time.Instant.parse(it).toEpochMilli() },
    snippet = snippet,
    attachmentsJson = if (attachments.isEmpty()) "" else gson.toJson(attachments),
    syncStatus = syncStatus
)
