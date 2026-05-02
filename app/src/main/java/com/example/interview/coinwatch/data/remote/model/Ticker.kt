package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class Ticker(
    @SerializedName("base")
    val base: String,
    @SerializedName("bid_ask_spread_percentage")
    val bidAskSpreadPercentage: Double,
    @SerializedName("coin_id")
    val coinId: String,
    @SerializedName("coin_mcap_usd")
    val coinMcapUsd: Double,
    @SerializedName("converted_last")
    val convertedLast: ConvertedLast,
    @SerializedName("converted_volume")
    val convertedVolume: ConvertedVolume,
    @SerializedName("is_anomaly")
    val isAnomaly: Boolean,
    @SerializedName("is_stale")
    val isStale: Boolean,
    @SerializedName("last")
    val last: Double,
    @SerializedName("last_fetch_at")
    val lastFetchAt: String,
    @SerializedName("last_traded_at")
    val lastTradedAt: String,
    @SerializedName("market")
    val market: Market,
    @SerializedName("target")
    val target: String,
    @SerializedName("target_coin_id")
    val targetCoinId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("token_info_url")
    val tokenInfoUrl: Any,
    @SerializedName("trade_url")
    val tradeUrl: String,
    @SerializedName("trust_score")
    val trustScore: Any,
    @SerializedName("volume")
    val volume: Double
)