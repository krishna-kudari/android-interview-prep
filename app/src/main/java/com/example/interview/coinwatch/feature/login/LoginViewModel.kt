package com.example.interview.coinwatch.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState = _formState.asStateFlow()

    fun onEmailChanged(value: String) {
        _formState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChanged(value: String) {
        _formState.update { it.copy(password = value, passwordError = null) }
    }

    fun onEmailFocusLost() {
        _formState.update { it.copy(emailError = validateEmail(it.email)) }
    }

    fun onPasswordFocusLost() {
        _formState.update { it.copy(passwordError = validatePassword(it.password)) }
    }

    fun onForgotPasswordClick() {
        _formState.update { it.copy(password = "", passwordError = null) }
    }


    fun onSubmit() {
        val emailError = validateEmail(_formState.value.email)
        val passwordError = validatePassword(_formState.value.password)

        if (emailError != null || passwordError != null) {
            _formState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, submitResult = null) }
            delay(1000)
            _formState.update {
                it.copy(
                    isSubmitting = false,
                    submitResult = if (Math.random() < 0.6) SubmitResult.Success else SubmitResult.Error(
                        "Invalid Credentials"
                    )
                )
            }
            delay(3000)
            _formState.update {
                it.copy(submitResult = null)
            }
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Minimum 6 characters"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> {
            "Email cannot be empty"
        }

        email.contains("@").not() -> {
            "Please enter a valid email"
        }

        else -> {
            null
        }
    }
}