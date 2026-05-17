package com.example.memos.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memos.data.db.dao.MemoDao
import com.example.memos.data.db.entity.MemoEntity

@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
abstract class MemosDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
}
