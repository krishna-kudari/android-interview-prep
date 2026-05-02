package com.example.interview.pulsenews.data.remote.model

import kotlin.time.Instant

data class ArticleDto(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val sourceName: String,
    val publishedAt: String,
    val isBookmarked: Boolean = false,
    val imageUrl: String,
    val url: String
)
