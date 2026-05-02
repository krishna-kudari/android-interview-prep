package com.example.interview.pulsenews.data.remote.model

data class ArticlesResponse(
    val articles: List<ArticleDto>,
    val totalResults: Int,
    val page: Int,
    val pageSize: Int,
)
