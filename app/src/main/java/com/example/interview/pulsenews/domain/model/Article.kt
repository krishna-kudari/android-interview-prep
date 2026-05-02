package com.example.interview.pulsenews.domain.model

data class Article(
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
