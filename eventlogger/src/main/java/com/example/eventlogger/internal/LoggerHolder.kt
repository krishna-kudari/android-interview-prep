package com.example.eventlogger.internal

import com.example.eventlogger.api.LoggerConfig
import com.example.eventlogger.persistence.EventDao

data class LoggerDependencies(
    val dao: EventDao,
    val config: LoggerConfig,
)

object LoggerHolder {

    @Volatile
    private var dependencies: LoggerDependencies? = null

    fun install(dependencies: LoggerDependencies) {
        this.dependencies = dependencies
    }

    fun require(): LoggerDependencies =
        checkNotNull(dependencies) { "EventLogger not initialized. Call EventLoggerFactory.create() first." }
}
