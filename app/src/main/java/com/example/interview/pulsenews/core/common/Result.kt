package com.example.interview.pulsenews.core.common

import android.os.Message

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = exception.localizedMessage) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit) : Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, String? ) -> Unit) : Result<T> {
    if (this is Result.Error) action(exception, message)
    return this
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(e)
}