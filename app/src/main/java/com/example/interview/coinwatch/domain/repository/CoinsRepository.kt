package com.example.interview.coinwatch.domain.repository

import androidx.paging.PagingData
import com.example.interview.coinwatch.domain.model.Coin
import kotlinx.coroutines.flow.Flow


interface CoinsRepository {
    fun getCoinsFeed(): Flow<PagingData<Coin>>
    fun searchCoins(query: String): Flow<PagingData<Coin>>
}