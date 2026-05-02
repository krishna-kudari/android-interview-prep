package com.example.interview.coinwatch.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNotificationsToggle(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            _events.send(
                UiEvent.ShowSnackBar(
                    if (enabled) "Notifications enabled" else "Notifications disabled"
                )
            )
        }
    }

    fun onDeleteAccountClicked() {
        viewModelScope.launch {
            _events.send(UiEvent.ShowDeleteConfirmation)
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            delay(1500)
            _events.send(UiEvent.ShowSnackBar("Account Deleted"))
            delay(800)
            _uiState.update { it.copy(isDeleting = false) }
            _events.send(UiEvent.NavigateBack)
        }
    }
}