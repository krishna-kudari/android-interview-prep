package com.example.interview.pulsenews.domain.repository

import androidx.paging.PagingData
import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNewsFeed(): Flow<PagingData<Article>>
    fun searchArticles(query: String): Flow<PagingData<Article>>
    suspend fun getArticle(id: String): Result<Article>
    fun observerBookmarks(): Flow<List<Article>>
    fun isBookmarked(articleId: String): Flow<Boolean>
    suspend fun toggleBookmark(article: Article)
}