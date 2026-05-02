package com.example.interview.coinwatch.domain.usecase

import androidx.paging.PagingData
import com.example.interview.coinwatch.domain.model.Coin
import com.example.interview.coinwatch.domain.repository.CoinsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCoinsUseCase @Inject constructor(
    private val repository: CoinsRepository
) {
    operator fun invoke(query: String) : Flow<PagingData<Coin>> = repository.searchCoins(query)
}