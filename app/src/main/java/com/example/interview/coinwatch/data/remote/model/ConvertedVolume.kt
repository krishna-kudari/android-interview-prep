package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class ConvertedVolume(
    @SerializedName("btc")
    val btc: Double,
    @SerializedName("eth")
    val eth: Double,
    @SerializedName("usd")
    val usd: Long
)