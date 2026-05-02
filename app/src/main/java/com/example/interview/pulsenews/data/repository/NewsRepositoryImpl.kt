package com.example.interview.pulsenews.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.core.common.safeApiCall
import com.example.interview.pulsenews.data.local.dao.BookmarkDao
import com.example.interview.pulsenews.data.local.mapper.toDomain
import com.example.interview.pulsenews.data.local.mapper.toEntity
import com.example.interview.pulsenews.data.remote.api.FakeNewsApi
import com.example.interview.pulsenews.data.remote.mapper.toDomain
import com.example.interview.pulsenews.data.remote.paging.NewsPagingSource
import com.example.interview.pulsenews.data.remote.paging.SearchPagingSource
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val api: FakeNewsApi,
    private val bookmarkDao: BookmarkDao
) : NewsRepository {

    private val pagingConfig: PagingConfig = PagingConfig(
        pageSize = 10,
        initialLoadSize = 10,
        prefetchDistance = 3,
        enablePlaceholders = false
    )

    override fun getNewsFeed(): Flow<PagingData<Article>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { NewsPagingSource(api) }
    ).flow

    override fun searchArticles(query: String): Flow<PagingData<Article>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { SearchPagingSource(api, query) }
    ).flow

    override suspend fun getArticle(id: String): Result<Article> = safeApiCall {
        api.getArticle(id).toDomain()
    }

    override fun observerBookmarks(): Flow<List<Article>> =
        bookmarkDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun isBookmarked(articleId: String): Flow<Boolean> =
        bookmarkDao.observeById(articleId).map { it != null }

    override suspend fun toggleBookmark(article: Article) {
        if (bookmarkDao.getById(article.id) != null) {
            bookmarkDao.deleteById(article.id)
        } else {
            bookmarkDao.insert(entity = article.toEntity())
        }
    }
}