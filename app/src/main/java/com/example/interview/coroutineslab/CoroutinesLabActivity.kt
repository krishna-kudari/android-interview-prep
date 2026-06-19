package com.example.interview.coroutineslab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.interview.compose.ui.theme.InterviewTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber

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
    fun main() {
        val scope = CoroutineScope(
            context = Dispatchers.Main.immediate
                    + SupervisorJob()
                    + CoroutineName("My scope")
                    + CoroutineExceptionHandler { context, throwable -> Timber.tag("TAG").e(throwable.message ?: "") }
        )

        scope.launch {
            withTimeout(5000) {

            }
        }

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
}


