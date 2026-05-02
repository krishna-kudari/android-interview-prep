package com.example.interview.pulsenews.data.remote.mapper

import com.example.interview.pulsenews.data.remote.model.ArticleDto
import com.example.interview.pulsenews.domain.model.Article
import kotlin.time.Instant

fun ArticleDto.toDomain(): Article = Article(
    id = id,
    title = title,
    description = description,
    content = content,
    author = author,
    sourceName = sourceName,
    publishedAt = publishedAt,
    isBookmarked = isBookmarked,
    imageUrl = imageUrl,
    url = url
)
