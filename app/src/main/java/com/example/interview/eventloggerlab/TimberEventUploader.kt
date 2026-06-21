package com.example.interview.eventloggerlab

import com.example.eventlogger.api.EventUploader
import com.example.eventlogger.api.UploadResult
import com.example.eventlogger.model.LogEvent
import timber.log.Timber

class TimberEventUploader : EventUploader {
    override suspend fun upload(events: List<LogEvent>): UploadResult {
        events.forEach { event ->
            Timber.i("EventUpload type=%s id=%s payload=%s", event.type, event.id, event.payload)
        }
        return UploadResult.Success
    }
}
