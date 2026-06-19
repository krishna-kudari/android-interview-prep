package com.example.interview.stopwatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.interview.stopwatch.repository.StopwatchRepository
import com.example.interview.stopwatch.ui.StopwatchScreen
import com.example.interview.stopwatch.ui.StopwatchViewModel

/**
 * Thin host Activity — responsible for:
 *  1. Requesting POST_NOTIFICATIONS permission (Android 13+).
 *  2. Binding to / unbinding from the service in onStart/onStop.
 *  3. Providing the ViewModel via the manual factory.
 *
 * WHY BIND IN onStart/onStop, NOT IN ViewModel
 * ─────────────────────────────────────────────
 * bindService() takes a Context. Using applicationContext would work for lifetime but
 * the ServiceConnection callback delivers results on the main thread of the process,
 * and leaking an Activity via its Context through a ServiceConnection across rotations
 * is a well-known memory-leak pattern. Keeping bind/unbind here ties connection
 * lifetime exactly to the visible window of this Activity, which is correct.
 *
 * The ViewModel survives rotation; the repository's connected flag drops briefly and
 * the stateIn(WhileSubscribed(5_000)) in the ViewModel absorbs the gap without a
 * cold restart.
 */
class StopwatchActivity : ComponentActivity() {

    // Application-scoped repository — one instance for the whole process.
    // We pass applicationContext so the repository never holds an Activity reference.
    private val repository by lazy { StopwatchRepository(applicationContext) }

    private val viewModel: StopwatchViewModel by viewModels {
        StopwatchViewModel.Factory(repository)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            StopwatchScreen(viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        repository.bind()
    }

    override fun onStop() {
        super.onStop()
        repository.unbind()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
