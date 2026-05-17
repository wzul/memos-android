package com.example.memos.data.repository

import com.example.memos.data.model.Memo
import com.example.memos.data.model.Visibility
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun observeMemos(): Flow<List<Memo>>
    fun searchMemos(query: String): Flow<List<Memo>>
    suspend fun getMemo(name: String): Memo?
    suspend fun createMemo(content: String, visibility: Visibility): Result<Memo>
    suspend fun updateMemo(name: String, content: String?, visibility: Visibility?, pinned: Boolean?): Result<Memo>
    suspend fun deleteMemo(name: String): Result<Unit>
    suspend fun sync(): Result<Unit>
    suspend fun clearLocalData()
}
