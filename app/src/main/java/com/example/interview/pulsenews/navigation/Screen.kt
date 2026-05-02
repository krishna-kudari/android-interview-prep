package com.example.interview.pulsenews.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Bookmarks : Screen("bookmarks")

    object Detail : Screen("detail/{articleId}") {
        fun createRoute(articleId: String) = "detail/$articleId"
        const val ARG_ARTICLE_ID = "articleId"
    }
}