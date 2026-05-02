package com.example.interview.coinwatch.data.remote.model


import com.google.gson.annotations.SerializedName

data class DeveloperData(
    @SerializedName("closed_issues")
    val closedIssues: Int,
    @SerializedName("code_additions_deletions_4_weeks")
    val codeAdditionsDeletions4Weeks: CodeAdditionsDeletions4Weeks,
    @SerializedName("commit_count_4_weeks")
    val commitCount4Weeks: Int,
    @SerializedName("forks")
    val forks: Int,
    @SerializedName("last_4_weeks_commit_activity_series")
    val last4WeeksCommitActivitySeries: List<Any>,
    @SerializedName("pull_request_contributors")
    val pullRequestContributors: Int,
    @SerializedName("pull_requests_merged")
    val pullRequestsMerged: Int,
    @SerializedName("stars")
    val stars: Int,
    @SerializedName("subscribers")
    val subscribers: Int,
    @SerializedName("total_issues")
    val totalIssues: Int
)