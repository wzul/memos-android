package com.example.memos.data.repository

import com.example.memos.data.api.MemosApi
import com.example.memos.data.api.dto.CreateMemoRequestDto
import com.example.memos.data.api.dto.MemoDto
import com.example.memos.data.api.dto.UpdateMemoRequestDto
import com.example.memos.data.api.dto.toEntity
import com.example.memos.data.db.dao.MemoDao
import com.example.memos.data.db.entity.MemoEntity
import com.example.memos.data.db.entity.SyncStatus
import com.example.memos.data.db.entity.toDomain
import com.example.memos.data.db.entity.toEntity
import com.example.memos.data.model.Attachment
import com.example.memos.data.model.Memo
import com.example.memos.data.model.Visibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    private val api: MemosApi,
    private val dao: MemoDao
) : MemoRepository {

    override fun observeMemos(): Flow<List<Memo>> =
        dao.observeMemos("NORMAL").map { list -> list.map { it.toDomain() } }

    override fun searchMemos(query: String): Flow<List<Memo>> =
        dao.searchMemos(query, "NORMAL").map { list -> list.map { it.toDomain() } }

    override suspend fun getMemo(name: String): Memo? =
        dao.getMemoByName(name)?.toDomain()

    override suspend fun refreshMemo(name: String): Result<Memo> = runCatching {
        val response = api.getMemo(name)
        if (!response.isSuccessful) throw IllegalStateException("Failed to fetch memo")
        val dto = response.body() ?: throw IllegalStateException("Empty response")
        val entity = dto.toEntity()
        dao.insertOrUpdate(entity)

        // Fetch attachments separately if not included in memo response
        if (dto.attachments.isNullOrEmpty()) {
            val attachResp = api.listMemoAttachments(name)
            if (attachResp.isSuccessful) {
                val attachList = attachResp.body()?.attachments?.map {
                    Attachment(
                        name = it.name ?: "",
                        filename = it.filename ?: "",
                        type = it.type,
                        size = it.size
                    )
                } ?: emptyList()
                if (attachList.isNotEmpty()) {
                    val updated = entity.copy(
                        attachmentsJson = com.google.gson.Gson().toJson(attachList)
                    )
                    dao.insertOrUpdate(updated)
                }
            }
        }

        dao.getMemoByName(name)?.toDomain()
            ?: throw IllegalStateException("Memo not found after refresh")
    }

    override suspend fun createMemo(content: String, visibility: Visibility): Result<Memo> = runCatching {
        val localId = "local-${UUID.randomUUID()}"
        val localMemo = MemoEntity(
            name = localId,
            localId = localId,
            content = content,
            state = "NORMAL",
            visibility = visibility.name,
            syncStatus = SyncStatus.PENDING_CREATE.name
        )
        dao.insertOrUpdate(localMemo)

        // Try sync immediately if online
        val resultEntity = syncSingle(localMemo)

        (resultEntity ?: dao.getMemoByName(localMemo.name))?.toDomain()
            ?: throw IllegalStateException("Failed to create memo")
    }

    override suspend fun updateMemo(
        name: String,
        content: String?,
        visibility: Visibility?,
        pinned: Boolean?
    ): Result<Memo> = runCatching {
        val entity = dao.getMemoByName(name)
            ?: throw IllegalStateException("Memo not found")

        val updated = entity.copy(
            content = content ?: entity.content,
            visibility = visibility?.name ?: entity.visibility,
            pinned = pinned ?: entity.pinned,
            syncStatus = if (entity.syncStatus == SyncStatus.PENDING_CREATE.name)
                SyncStatus.PENDING_CREATE.name
            else
                SyncStatus.PENDING_UPDATE.name,
            lastModified = System.currentTimeMillis()
        )
        dao.insertOrUpdate(updated)
        val resultEntity = syncSingle(updated)

        (resultEntity ?: dao.getMemoByName(name))?.toDomain()
            ?: throw IllegalStateException("Failed to update memo")
    }

    override suspend fun deleteMemo(name: String): Result<Unit> = runCatching {
        val entity = dao.getMemoByName(name) ?: return@runCatching
        val updated = entity.copy(
            isDeleted = true,
            syncStatus = if (entity.syncStatus == SyncStatus.PENDING_CREATE.name)
                SyncStatus.PENDING_CREATE.name
            else
                SyncStatus.PENDING_DELETE.name
        )
        dao.insertOrUpdate(updated)
        syncSingle(updated)
    }

    override suspend fun sync(): Result<Unit> = runCatching {
        // Push pending changes first
        val pending = dao.getPendingSyncMemos()
        pending.forEach { syncSingle(it) }

        // Pull remote changes
        var pageToken: String? = null
        do {
            val response = api.listMemos(pageSize = 100, pageToken = pageToken)
            if (!response.isSuccessful) break
            val body = response.body() ?: break
            val remoteMemos = body.memos ?: emptyList()
            val entities = remoteMemos.map { it.toEntity() }
            dao.insertOrUpdateAll(entities)
            pageToken = body.nextPageToken
        } while (!pageToken.isNullOrBlank())

        dao.purgeSyncedDeletes()
    }

    override suspend fun clearLocalData() {
        dao.clearAll()
    }

    private suspend fun syncSingle(entity: MemoEntity): MemoEntity? {
        return when (SyncStatus.valueOf(entity.syncStatus)) {
            SyncStatus.PENDING_CREATE -> {
                val request = CreateMemoRequestDto(
                    content = entity.content,
                    visibility = entity.visibility,
                    pinned = entity.pinned,
                    tags = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",")
                )
                val resp = api.createMemo(request)
                if (resp.isSuccessful) {
                    val remote = resp.body()
                    if (remote?.name != null) {
                        dao.deleteByName(entity.name)
                        val synced = entity.copy(
                            name = remote.name,
                            localId = null,
                            syncStatus = SyncStatus.SYNCED.name
                        )
                        dao.insertOrUpdate(synced)
                        return synced
                    }
                }
                entity
            }
            SyncStatus.PENDING_UPDATE -> {
                val updateMask = buildList {
                    add("content")
                    add("visibility")
                    add("pinned")
                }.joinToString(",")
                val request = UpdateMemoRequestDto(
                    content = entity.content,
                    visibility = entity.visibility,
                    pinned = entity.pinned
                )
                val resp = api.updateMemo(entity.name, updateMask, request)
                if (resp.isSuccessful) {
                    val synced = entity.copy(syncStatus = SyncStatus.SYNCED.name)
                    dao.insertOrUpdate(synced)
                    return synced
                }
                entity
            }
            SyncStatus.PENDING_DELETE -> {
                val resp = api.deleteMemo(entity.name)
                if (resp.isSuccessful || resp.code() == 404) {
                    dao.deleteByName(entity.name)
                    return null
                }
                entity
            }
            SyncStatus.SYNCED -> entity
        }
    }
}
