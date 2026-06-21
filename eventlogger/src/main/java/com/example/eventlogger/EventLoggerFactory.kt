package com.example.eventlogger

import android.content.Context
import com.example.eventlogger.api.EventLogger
import com.example.eventlogger.api.LoggerConfig
import com.example.eventlogger.core.EventLoggerImpl
import com.example.eventlogger.persistence.EventDatabase

object EventLoggerFactory {

    @Volatile
    private var instance: EventLogger? = null

    fun create(context: Context, config: LoggerConfig): EventLogger {
        return synchronized(this) {
            instance ?: build(context.applicationContext, config).also { instance = it }
        }
    }

    fun getOrNull(): EventLogger? = instance

    fun require(): EventLogger =
        checkNotNull(instance) { "EventLogger not initialized. Call EventLoggerFactory.create() first." }

    private fun build(context: Context, config: LoggerConfig): EventLogger {
        val dao = EventDatabase.create(context).eventDao()
        return EventLoggerImpl(
            appContext = context,
            config = config,
            dao = dao,
        )
    }
}
