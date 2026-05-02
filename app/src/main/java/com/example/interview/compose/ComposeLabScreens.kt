package com.example.interview.compose

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.interview.R

@Composable
fun ComposeLabHomeScreen(
    onOpenStateSandbox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.compose_lab_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Text(
            text = stringResource(R.string.compose_lab_intro),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onOpenStateSandbox) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.compose_lab_open_state))
        }
    }
}

@Composable
fun ComposeLabStateSandboxScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComposeLabViewModel = viewModel(),
) {
    val counter by viewModel.counter.collectAsStateWithLifecycle()
    val liveText by viewModel.liveLabel.observeAsState(initial = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.compose_lab_state_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.compose_lab_stateflow_line, counter),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = viewModel::incrementCounter) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.compose_lab_increment))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = liveText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = viewModel::refreshLiveLabel) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.compose_lab_refresh_livedata))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.compose_lab_back))
        }
    }
}


@Composable
fun LazyListExample(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit
) {
    val items = remember { List(1000) {
        "Item $it"
    } }

    val itemsList = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = items, key = { it }) { item ->
            LazyItem(item)
        }
    }
}

@Composable
fun LazyItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
