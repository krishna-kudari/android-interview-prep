package com.example.interview.coinwatch.feature.settings

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val isDeleting: Boolean = false
)