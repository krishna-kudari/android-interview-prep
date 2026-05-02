package com.example.interview.pulsenews

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.interview.pulsenews.core.ui.theme.PulseNewsTheme
import com.example.interview.pulsenews.navigation.PulseNewsNavGraph
import com.example.interview.pulsenews.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseNewsTheme {
                PulseNewsApp()
            }
        }
    }
}


@Composable
fun PulseNewsApp() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Feed, "Feed", Icons.Default.Home),
        BottomNavItem(Screen.Bookmarks, "Bookmarks", Icons.Default.Bookmarks)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { paddingValues ->
        PulseNewsNavGraph(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
        )
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)