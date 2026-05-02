package com.example.interview.pulsenews.feature.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.interview.pulsenews.core.ui.components.ErrorView
import com.example.interview.pulsenews.core.ui.components.ShimmerArticleCard
import com.example.interview.pulsenews.feature.feed.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onArticleClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()

    val feedItems = viewModel.feedPagingData.collectAsLazyPagingItems()
    val searchItems = viewModel.searchPagingData.collectAsLazyPagingItems()
    val pagingData = if (isSearchActive) searchItems else feedItems

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Column() {
                TopAppBar(title = { Text("Pulse News") })

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search Articles...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchCleared()
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close search"
                                )
                            }
                        }
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
            isRefreshing = pagingData.loadState.refresh is LoadState.Loading,
            onRefresh = { pagingData.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val refresh = pagingData.loadState.refresh) {
                    is LoadState.Loading -> {
                        items(6) {
                            ShimmerArticleCard()
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            ErrorView(
                                message = refresh.error.localizedMessage ?: "Something went wrong",
                                onRetry = { pagingData.retry() }
                            )
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (pagingData.itemCount == 0) {
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
                    count = pagingData.itemCount,
                    key = { index -> pagingData.peek(index)?.id ?: index }
                ) { index ->
                    pagingData[index]?.let { article ->
                        ArticleCard(
                            modifier = Modifier.fillMaxSize(),
                            article = article,
                            isBookmarked = article.id in bookmarkedIds,
                            onArticleClick = onArticleClick,
                            onBookmarkToggle = viewModel::onToggleBookmark
                        )
                    }
                }

                when (val append = pagingData.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = append.error.localizedMessage ?: "Failed to load more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { pagingData.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

