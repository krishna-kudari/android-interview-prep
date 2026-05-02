package com.example.interview.pulsenews.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.usecase.GetBookmarksUseCase
import com.example.interview.pulsenews.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) : ViewModel() {

    val uiState: StateFlow<BookmarksUiState> = getBookmarksUseCase()
        .map {
            if (it.isEmpty()) BookmarksUiState.Empty
            else BookmarksUiState.Success(it)
        }.onStart {
            emit(BookmarksUiState.Loading)
        }.stateIn(
            scope = viewModelScope,
            initialValue = BookmarksUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun removeBookmark(article: Article) {
        viewModelScope.launch {
            toggleBookmarkUseCase(article)
        }
    }
}