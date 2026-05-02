package com.example.interview.pulsenews.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interview.pulsenews.data.local.entity.BookMarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<BookMarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id=:id")
    fun observeById(id: String): Flow<BookMarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE id=:id")
    suspend fun getById(id: String): BookMarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookMarkEntity)

    @Query("DELETE FROM bookmarks WHERE id=:id")
    suspend fun deleteById(id: String)
}