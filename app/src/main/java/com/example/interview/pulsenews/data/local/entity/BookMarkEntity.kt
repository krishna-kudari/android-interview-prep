package com.example.interview.pulsenews.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookMarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val sourceName: String,
    val publishedAt: String,
    val imageUrl: String,
    val url: String,
    val savedAt: Long = System.currentTimeMillis()
)