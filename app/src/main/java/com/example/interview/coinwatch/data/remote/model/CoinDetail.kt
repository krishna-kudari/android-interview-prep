package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class CoinDetailDto(
    @SerializedName("additional_notices")
    val additionalNotices: List<Any?>,
    @SerializedName("asset_platform_id")
    val assetPlatformId: Any,
    @SerializedName("block_time_in_minutes")
    val blockTimeInMinutes: Int,
    @SerializedName("categories")
    val categories: List<String>,
    @SerializedName("community_data")
    val communityData: CommunityData,
    @SerializedName("country_origin")
    val countryOrigin: String,
    @SerializedName("description")
    val description: Description,
    @SerializedName("detail_platforms")
    val detailPlatforms: DetailPlatforms,
    @SerializedName("developer_data")
    val developerData: DeveloperData,
    @SerializedName("genesis_date")
    val genesisDate: String,
    @SerializedName("hashing_algorithm")
    val hashingAlgorithm: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("image")
    val image: Image,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("links")
    val links: Links,
    @SerializedName("market_cap_rank")
    val marketCapRank: Int,
    @SerializedName("market_cap_rank_with_rehypothecated")
    val marketCapRankWithRehypothecated: Int,
    @SerializedName("market_data")
    val marketData: MarketData,
    @SerializedName("name")
    val name: String,
    @SerializedName("platforms")
    val platforms: Platforms,
    @SerializedName("preview_listing")
    val previewListing: Boolean,
    @SerializedName("public_notice")
    val publicNotice: Any,
    @SerializedName("sentiment_votes_down_percentage")
    val sentimentVotesDownPercentage: Double,
    @SerializedName("sentiment_votes_up_percentage")
    val sentimentVotesUpPercentage: Double,
    @SerializedName("status_updates")
    val statusUpdates: List<Any?>,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("tickers")
    val tickers: List<Ticker>,
    @SerializedName("watchlist_portfolio_users")
    val watchlistPortfolioUsers: Int,
    @SerializedName("web_slug")
    val webSlug: String
)