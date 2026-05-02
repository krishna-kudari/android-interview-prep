package com.example.interview.coinwatch.feature.profile

import com.example.interview.coinwatch.domain.model.UserProfile

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data object Idle : ProfileUiState()
    data class Error(val message: String, val errorCount: Int) : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
}