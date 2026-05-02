package com.example.interview.pulsenews.feature.detail

import android.os.Message
import com.example.interview.pulsenews.domain.model.Article

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val article: Article) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}