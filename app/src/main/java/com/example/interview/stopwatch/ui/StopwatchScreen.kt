package com.example.interview.stopwatch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StopwatchScreen(viewModel: StopwatchViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StopwatchContent(
        uiState = uiState,
        onStartPause = viewModel::onStartPauseClicked,
        onReset = viewModel::onResetClicked
    )
}

/**
 * Stateless inner composable — easy to preview and test in isolation.
 */
@Composable
private fun StopwatchContent(
    uiState: StopwatchUiState,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elapsed time display
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.displayTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = 64.sp,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(56.dp))

            // Controls row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset — disabled when Idle
                OutlinedButton(
                    onClick = onReset,
                    enabled = !uiState.isIdle,
                ) {
                    Text("Reset")
                }

                Spacer(Modifier.width(24.dp))

                // Start / Pause — animated label swap
                Button(
                    onClick = onStartPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRunning)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    AnimatedContent(
                        targetState = uiState.isRunning,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "start_pause_label"
                    ) { running ->
                        Text(if (running) "Pause" else "Start")
                    }
                }
            }
        }
    }
}
