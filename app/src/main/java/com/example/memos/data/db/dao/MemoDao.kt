package com.example.memos.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memos.data.db.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = :state ORDER BY pinned DESC, createTime DESC")
    fun observeMemos(state: String = "NORMAL"): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = :state AND (content LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%') ORDER BY pinned DESC, createTime DESC")
    fun searchMemos(query: String, state: String = "NORMAL"): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE name = :name LIMIT 1")
    suspend fun getMemoByName(name: String): MemoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(memo: MemoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(memos: List<MemoEntity>)

    @Query("DELETE FROM memos WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT * FROM memos WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncMemos(): List<MemoEntity>

    @Query("DELETE FROM memos WHERE isDeleted = 1 AND syncStatus = 'SYNCED'")
    suspend fun purgeSyncedDeletes()

    @Query("DELETE FROM memos")
    suspend fun clearAll()
}
