package com.example.interview.coinwatch.feature.settings

sealed class UiEvent {
    data class ShowSnackBar(val message: String) : UiEvent()
    data object ShowDeleteConfirmation : UiEvent()
    data object NavigateBack : UiEvent()
}