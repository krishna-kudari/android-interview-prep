package com.example.interview.coinwatch.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.interview.coinwatch.data.remote.api.CoinGeckoApi
import com.example.interview.coinwatch.data.remote.mapper.toDomain
import com.example.interview.coinwatch.domain.model.Coin

class CoinsPagingSource(
    private val api: CoinGeckoApi
) : PagingSource<Int, Coin>() {

    companion object {
        const val STARTING_PAGE = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Coin> {
        return try {
            val page = params.key ?: STARTING_PAGE
            val pageSize = params.loadSize
            val response = api.getCoins(page = page, pageSize = pageSize)
            val coinListResponseItems = response.body().orEmpty()
            LoadResult.Page(
                data = coinListResponseItems.map { it.toDomain() },
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (coinListResponseItems.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Coin>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}