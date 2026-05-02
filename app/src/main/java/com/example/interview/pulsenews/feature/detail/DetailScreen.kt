package com.example.interview.pulsenews.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.interview.pulsenews.core.common.toRelativeTime
import com.example.interview.pulsenews.core.ui.components.ErrorView
import com.example.interview.pulsenews.core.ui.theme.BookmarkYellow
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.feature.detail.testutil.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    articleId: String,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text("Article")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag(TestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        val article = (uiState as DetailUiState.Success).article
                        IconButton(
                            onClick = { viewModel.toggleBookmark(article) },
                            modifier = Modifier.testTag(TestTags.BOOKMARK_ICON)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                                tint = if (isBookmarked) BookmarkYellow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag(TestTags.DETAIL_LOADING),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = Modifier
                        .padding(padding)
                        .testTag(TestTags.DETAIL_ERROR),
                    onRetry = viewModel::retry
                )
            }

            is DetailUiState.Success -> {
                ArticleContent(
                    modifier = Modifier
                        .padding(padding)
                        .testTag(TestTags.DETAIL_CONTENT),
                    article = state.article
                )
            }
        }
    }
}

@Composable
fun ArticleContent(
    modifier: Modifier = Modifier,
    article: Article
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = article.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(article.sourceName)
                    }
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(article.publishedAt.toRelativeTime())
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By ${article.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                modifier = Modifier.padding(12.dp)
            )

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
