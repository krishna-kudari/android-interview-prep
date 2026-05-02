package com.example.interview.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.interview.compose.ui.theme.InterviewTheme

class ComposeLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewTheme(darkTheme = true) {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = ComposeLabDestinations.LAZY_LIST,
                        modifier = Modifier.padding(paddingValues = innerPadding)
                    ) {
                        composable(route = ComposeLabDestinations.HOME) {
                            ComposeLabHomeScreen(
                                onOpenStateSandbox = {
                                    navController.navigate(ComposeLabDestinations.STATE_SANDBOX)
                                }
                            )
                        }

                        composable(route = ComposeLabDestinations.STATE_SANDBOX) {
                            ComposeLabStateSandboxScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }

                        composable(route = ComposeLabDestinations.LAZY_LIST) {
                            LazyListExample(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
