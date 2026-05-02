package com.example.interview.coinwatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.interview.coinwatch.feature.feed.FeedScreen
import com.example.interview.coinwatch.feature.login.LoginScreen
import com.example.interview.coinwatch.feature.profile.UserProfileScreen
import com.example.interview.coinwatch.feature.settings.SettingsScreen


@Composable
fun CoinWatchNavGraph(
    modifier: Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Settings.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = {

                },
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route)
                }
            )
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                onCoinClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            UserProfileScreen(
                onBackClick = { navController.navigate(Screen.Feed.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigate(Screen.Login.route) }
            )
        }
    }
}