package com.example.interview.pulsenews.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.usecase.GetBookmarksUseCase
import com.example.interview.pulsenews.domain.usecase.GetNewsFeedUseCase
import com.example.interview.pulsenews.domain.usecase.SearchArticlesUseCase
import com.example.interview.pulsenews.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getNewsFeedUseCase: GetNewsFeedUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase,
    private val getBookmarksUseCase: GetBookmarksUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isSearchActive: StateFlow<Boolean> = _searchQuery.map {
        it.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val feedPagingData: Flow<PagingData<Article>> = getNewsFeedUseCase().cachedIn(viewModelScope)

    val searchPagingData: Flow<PagingData<Article>> =
        _searchQuery.debounce(300).filter { it.isNotBlank() }
            .flatMapLatest { searchArticlesUseCase(it) }.cachedIn(viewModelScope)

    val bookmarkedIds: StateFlow<Set<String>> =
        getBookmarksUseCase().map { bookmarks -> bookmarks.map { it.id }.toSet() }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchCleared() {
        _searchQuery.value = ""
    }

    fun onToggleBookmark(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleBookmarkUseCase(article)
        }
    }
}