package com.example.interview.coinwatch.domain.usecase

import androidx.paging.PagingData
import com.example.interview.coinwatch.domain.model.Coin
import com.example.interview.coinwatch.domain.repository.CoinsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoinsFeedUseCase @Inject constructor(
    private val repository: CoinsRepository
) {
    operator fun invoke(): Flow<PagingData<Coin>> = repository.getCoinsFeed()
}