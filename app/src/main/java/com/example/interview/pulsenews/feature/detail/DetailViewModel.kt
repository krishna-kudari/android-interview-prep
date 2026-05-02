package com.example.interview.pulsenews.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.usecase.GetArticleDetailUseCase
import com.example.interview.pulsenews.domain.usecase.IsBookmarkedUseCase
import com.example.interview.pulsenews.domain.usecase.ToggleBookmarkUseCase
import com.example.interview.pulsenews.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val isBookmarkedUseCase: IsBookmarkedUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getArticleUseCase: GetArticleDetailUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val articleId: String = checkNotNull(
        savedStateHandle[Screen.Detail.ARG_ARTICLE_ID]
    )

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> get() = _uiState.asStateFlow()

    val isBookmarked: StateFlow<Boolean> = isBookmarkedUseCase(articleId).stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(5_000)
    )


    init {
        loadArticle()
    }

    fun loadArticle() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val result = getArticleUseCase(articleId)) {
                is Result.Success -> {
                    _uiState.value = DetailUiState.Success(result.data)
                }

                is Result.Error -> {
                    _uiState.value = DetailUiState.Error(result.message ?: "Failed to load Article")
                }

                is Result.Loading -> Unit
            }
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            toggleBookmarkUseCase(article)
        }
    }

    fun retry() = loadArticle()
}