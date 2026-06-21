package com.example.eventlogger.api

sealed class UploadResult {
    data object Success : UploadResult()

    data class Retryable(val reason: String) : UploadResult()

    data class Permanent(val reason: String) : UploadResult()
}
