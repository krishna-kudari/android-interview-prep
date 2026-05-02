package com.example.interview.pulsenews.feature.bookmarks

import com.example.interview.pulsenews.domain.model.Article

sealed class BookmarksUiState {
    data class Success(val articles: List<Article>) : BookmarksUiState()
    object Empty : BookmarksUiState()
    object Loading : BookmarksUiState()
}