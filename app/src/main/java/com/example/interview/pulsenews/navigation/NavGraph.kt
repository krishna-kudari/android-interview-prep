package com.example.interview.pulsenews.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.interview.pulsenews.feature.bookmarks.BookmarkScreen
import com.example.interview.pulsenews.feature.detail.DetailScreen
import com.example.interview.pulsenews.feature.feed.FeedScreen

@Composable
fun PulseNewsNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Feed.route
    ) {

        composable(
            route = Screen.Feed.route
        ) {
            FeedScreen(
                onArticleClick = { articleId: String ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.Bookmarks.route
        ) {
            BookmarkScreen(
                onArticleClick = { articleId: String ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument(Screen.Detail.ARG_ARTICLE_ID) { type = NavType.StringType },
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "pulsenews:://article/{articleId}" }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString(Screen.Detail.ARG_ARTICLE_ID)
                ?: return@composable
            DetailScreen(
                articleId = articleId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

    }
}