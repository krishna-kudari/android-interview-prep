package com.example.interview.pulsenews.data.local.mapper

import com.example.interview.pulsenews.data.local.entity.BookMarkEntity
import com.example.interview.pulsenews.domain.model.Article

fun BookMarkEntity.toDomain() = Article(
    id = id,
    title = title,
    description = description,
    content = content,
    author = author,
    sourceName = sourceName,
    publishedAt = publishedAt,
    isBookmarked = true,
    imageUrl = imageUrl,
    url = url
)

fun Article.toEntity() = BookMarkEntity(
    id = id,
    title = title,
    description = description,
    content = content,
    author = author,
    sourceName = sourceName,
    publishedAt = publishedAt,
    imageUrl = imageUrl,
    url = url
)