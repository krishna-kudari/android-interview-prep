package com.example.interview.datastorelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.interview.datastorelab.ui.DataStoreLabScreen
import com.example.interview.pulsenews.core.ui.theme.PulseNewsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataStoreLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseNewsTheme {
                DataStoreLabScreen()
            }
        }
    }
}
