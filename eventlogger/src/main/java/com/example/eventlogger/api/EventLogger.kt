package com.example.eventlogger.api

import com.example.eventlogger.model.LogEvent

interface EventLogger {

    fun log(
        type: String,
        properties: Map<String, Any?> = emptyMap(),
        priority: Int = LogEvent.PRIORITY_NORMAL,
    )

    suspend fun flush()

    suspend fun stats(): LoggerStats
}

data class LoggerStats(
    val pendingInDb: Int,
    val uploadingInDb: Int,
    val deadInDb: Int,
    val totalInDb: Int,
)
