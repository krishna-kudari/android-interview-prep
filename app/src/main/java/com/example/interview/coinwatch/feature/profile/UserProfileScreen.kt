package com.example.interview.coinwatch.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.interview.coinwatch.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfileState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Profile")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (val state = userProfileState) {
                is ProfileUiState.Idle -> {
                    Button(onClick = viewModel::fetchProfile) {
                        Text("Fetch Profile")
                    }
                }

                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Fetching Profile")
                }

                is ProfileUiState.Error -> {
                    Text("${state.message} - ${state.errorCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::fetchProfile) {
                        Text("Fetch Profile")
                    }
                }

                is ProfileUiState.Success -> {
                    ProfileCard(modifier = Modifier, state.profile)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::fetchProfile) {
                        Text("Refresh")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    profile: UserProfile
) {
    Column(
        modifier = modifier
            .padding(20.dp)
    ) {
        AsyncImage(
            model = profile.avatarUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = profile.name,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview
@Composable
fun PreviewProfileCard() {
    ProfileCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp), profile = UserProfile(
            name = "Lal Laadle",
            email = "Laadle@gmail.com",
            bio = "hat ja laadle",
            avatarUrl = "https://i.pravatar.cc/300"
        )
    )
}