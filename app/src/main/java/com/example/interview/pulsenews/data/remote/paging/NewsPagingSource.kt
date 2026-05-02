package com.example.interview.pulsenews.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.interview.pulsenews.data.remote.api.FakeNewsApi
import com.example.interview.pulsenews.data.remote.mapper.toDomain
import com.example.interview.pulsenews.domain.model.Article

class NewsPagingSource(
    private val api: FakeNewsApi
) : PagingSource<Int, Article>() {

    companion object {
        const val STARTING_PAGE = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: STARTING_PAGE
            val response = api.getArticles(page, params.loadSize)
            LoadResult.Page(
                data = response.articles.map { it.toDomain() },
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (response.articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}