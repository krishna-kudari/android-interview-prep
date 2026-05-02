package com.example.interview.coinwatch.data.remote.mapper

import com.example.interview.coinwatch.data.remote.model.CoinListResponseItem
import com.example.interview.coinwatch.domain.model.Coin

fun CoinListResponseItem.toDomain(): Coin = Coin(
    id = id,
    name = name,
    icon = image,
    symbol = symbol,
    currentPrice = currentPrice,
    marketCap = marketCap,
    priceChangePercentageIn24Hrs = priceChangePercentage24h,
    circulatingSupply = circulatingSupply,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    lastUpdated = lastUpdated
)