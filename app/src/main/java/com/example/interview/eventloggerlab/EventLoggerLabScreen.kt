package com.example.interview.eventloggerlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interview.pulsenews.core.ui.theme.PulseNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLoggerLabScreen(
    viewModel: EventLoggerLabViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PulseNewsTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Event Logger Lab") })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatsCard(uiState = uiState)

                LabButton(text = "Log single event") { viewModel.logSingleEvent() }
                LabButton(text = "Log HIGH priority") { viewModel.logHighPriorityEvent() }
                LabButton(text = "Log CRITICAL (direct to Room)") { viewModel.logCriticalEvent() }
                LabButton(text = "Burst 50 scroll events") { viewModel.logBurst(50) }
                LabButton(text = "Force flush") { viewModel.flush() }
                LabButton(text = "Refresh stats") { viewModel.refreshStats() }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Activity log", style = MaterialTheme.typography.titleMedium)
                uiState.messages.forEach { message ->
                    Text(
                        text = "• $message",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(uiState: EventLoggerLabUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Queue stats", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pending in Room: ${uiState.stats.pendingInDb}")
            Text("Uploading: ${uiState.stats.uploadingInDb}")
            Text("Dead letter: ${uiState.stats.deadInDb}")
            Text("Total in Room: ${uiState.stats.totalInDb}")
        }
    }
}

@Composable
private fun LabButton(
    text: String,
    onClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text)
        }
    }
}
