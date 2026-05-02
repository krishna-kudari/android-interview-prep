package com.example.interview.coinwatch.feature.login

data class LoginFormState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val submitResult: SubmitResult? = null
) {
    val isFormValid: Boolean
        get() = emailError == null && passwordError == null && email.isNotBlank() && password.isNotBlank()
}

sealed class SubmitResult {
    data object Success : SubmitResult()
    data class Error(val message: String) : SubmitResult()
}

