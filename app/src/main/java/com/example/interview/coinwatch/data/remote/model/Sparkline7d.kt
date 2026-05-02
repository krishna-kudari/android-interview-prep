package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class Sparkline7d(
    @SerializedName("price")
    val price: List<Double>
)