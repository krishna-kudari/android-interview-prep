package com.example.interview.coinwatch.feature.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.interview.coinwatch.feature.feed.components.CoinCard
import com.example.interview.pulsenews.core.ui.components.ErrorView
import com.example.interview.pulsenews.core.ui.components.ShimmerArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onCoinClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()

    val feedPagingData = viewModel.feedPagingData.collectAsLazyPagingItems()
    val searchPagingData = viewModel.searchPagingData.collectAsLazyPagingItems()
    val pagingItems = if (isSearchActive) searchPagingData else feedPagingData

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(title = { Text("Coin List") })

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Coins..."
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.onSearchCleared()
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                )
                            }
                        }
                    },
                    placeholder = {
                        Text("Search coins...")
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider()
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
            onRefresh = { pagingItems.refresh() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = pagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        items(6) {
                            ShimmerArticleCard()
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            ErrorView(
                                message = state.error.localizedMessage ?: "Something went wrong",
                                onRetry = pagingItems::refresh
                            )
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (pagingItems.itemCount == 0) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSearchActive) "No results for \"$searchQuery\"" else "No articles found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    count = pagingItems.itemCount,
                    key = { index: Int -> index }
                ) { index ->
                    pagingItems[index]?.let { coin ->
                        CoinCard(
                            modifier = Modifier,
                            coin = coin,
                            onCoinClick = onCoinClick
                        )
                    }
                }
            }
        }
    }
}