package com.example.interview.coinwatch.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.interview.coinwatch.data.remote.api.CoinGeckoApi
import com.example.interview.coinwatch.data.remote.paging.CoinsPagingSource
import com.example.interview.coinwatch.data.remote.paging.SearchPagingSource
import com.example.interview.coinwatch.domain.model.Coin
import com.example.interview.coinwatch.domain.repository.CoinsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi
) : CoinsRepository {

    private val pagingConfig: PagingConfig = PagingConfig(
        pageSize = 10,
        initialLoadSize = 10,
        prefetchDistance = 3,
        enablePlaceholders = false
    )

    override fun getCoinsFeed(): Flow<PagingData<Coin>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { CoinsPagingSource(api = api) }
    ).flow

    override fun searchCoins(query: String): Flow<PagingData<Coin>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { SearchPagingSource(api, query) }
        ).flow
    }
}