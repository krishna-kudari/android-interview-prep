package com.example.interview.pulsenews.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.interview.pulsenews.data.local.dao.BookmarkDao
import com.example.interview.pulsenews.data.local.entity.BookMarkEntity

@Database(
    entities = [BookMarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PulseNewsDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}