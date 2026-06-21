package com.example.eventlogger.api

import com.example.eventlogger.model.LogEvent

fun interface EventUploader {
    suspend fun upload(events: List<LogEvent>): UploadResult
}
