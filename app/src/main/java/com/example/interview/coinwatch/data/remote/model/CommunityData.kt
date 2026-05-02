package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class CommunityData(
    @SerializedName("facebook_likes")
    val facebookLikes: Any,
    @SerializedName("reddit_accounts_active_48h")
    val redditAccountsActive48h: Int,
    @SerializedName("reddit_average_comments_48h")
    val redditAverageComments48h: Int,
    @SerializedName("reddit_average_posts_48h")
    val redditAveragePosts48h: Int,
    @SerializedName("reddit_subscribers")
    val redditSubscribers: Int,
    @SerializedName("telegram_channel_user_count")
    val telegramChannelUserCount: Any
)