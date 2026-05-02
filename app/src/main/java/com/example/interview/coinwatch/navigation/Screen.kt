package com.example.interview.coinwatch.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object Settings: Screen("settings")
}