package com.example.interview.coinwatch.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interview.coinwatch.di.IoDispatcher
import com.example.interview.coinwatch.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _errorCount = MutableStateFlow(0)

    fun fetchProfile() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = profileRepository.getUserProfile()
                _uiState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                _errorCount.update { it + 1 }
                _uiState.value = ProfileUiState.Error(
                    message = e.message ?: "Unknown error",
                    errorCount = _errorCount.value,
                )
            }
        }
    }
}
