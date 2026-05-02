package com.example.interview.coinwatch.data.remote.api


import com.example.interview.coinwatch.data.remote.model.CoinDetailDto
import com.example.interview.coinwatch.data.remote.model.CoinListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface CoinGeckoApi {

    @GET("/api/v3/coins/markets/")
    suspend fun getCoins(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): Response<CoinListResponse>

    @GET("/api/v3/search/")
    suspend fun searchCoins(
        @Query("query") query: String,
        @Query("per_page") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): Response<CoinListResponse>

    @GET("/api/v3/coins/{coinId}")
    suspend fun coinDetail(
        @Path("coinId") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("sparkline") sparkLine: Boolean = true
    ): Response<CoinDetailDto>
}