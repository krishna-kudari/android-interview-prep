package com.example.interview.coroutineslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.interview.compose.ui.theme.InterviewTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class CoroutinesLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewTheme(darkTheme = true) {
                CoroutinesPlaygroundScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}


fun main() {
    runBlocking {
        println("start")
        launch { doSomethingAsync() }
        println("end")
    }
}
suspend fun doSomethingAsync() = withContext(Dispatchers.Main) {
    println("async work started...")
    delay(200)
    println("async work done!")
}