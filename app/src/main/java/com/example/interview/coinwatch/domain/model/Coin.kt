package com.example.interview.coinwatch.domain.model

data class Coin(
    val id: String,
    val name: String,
    val icon: String,
    val symbol: String,
    val currentPrice: Double,
    val marketCap: Long,
    val priceChangePercentageIn24Hrs: Double,
    val circulatingSupply: Double,
    val totalVolume: Long,
    val high24h: Double,
    val low24h: Double,
    val lastUpdated: String,
)