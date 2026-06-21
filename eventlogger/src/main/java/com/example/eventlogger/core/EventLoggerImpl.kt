package com.example.eventlogger.core

import android.content.ComponentCallbacks2
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.eventlogger.api.EventLogger
import com.example.eventlogger.api.LoggerConfig
import com.example.eventlogger.api.LoggerStats
import com.example.eventlogger.internal.LoggerDependencies
import com.example.eventlogger.internal.LoggerHolder
import com.example.eventlogger.model.LogEvent
import com.example.eventlogger.persistence.EventDao
import com.example.eventlogger.upload.UploadScheduler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

internal class EventLoggerImpl(
    private val appContext: Context,
    private val config: LoggerConfig,
    private val dao: EventDao,
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO.limitedParallelism(1) + CoroutineName("EventLogger"),
    ),
) : EventLogger {

    private val channel = Channel<LogEvent>(
        capacity = config.memoryQueueCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var foregroundUploadJob: Job? = null

    init {
        LoggerHolder.install(LoggerDependencies(dao = dao, config = config))
        scope.launch { recoverStaleUploads() }
        startBatchProcessor()
        UploadScheduler.schedulePeriodic(appContext, config)
        observeAppLifecycle()
        registerTrimMemoryCallback()
    }

    override fun log(
        type: String,
        properties: Map<String, Any?>,
        priority: Int,
    ) {
        val event = LogEvent(
            type = type,
            payload = JSONObject(properties).toString(),
            priority = priority,
        )

        if (priority >= LogEvent.PRIORITY_CRITICAL) {
            scope.launch {
                flushEventsToDb(listOf(event))
            }
            return
        }

        val result = channel.trySend(event)
        if (result.isFailure && priority >= LogEvent.PRIORITY_HIGH) {
            scope.launch { channel.send(event) }
        }
    }

    override suspend fun flush() {
        drainChannelToDb()
        UploadScheduler.enqueueOneTime(appContext, config, replaceExisting = true)
    }

    override suspend fun stats(): LoggerStats = LoggerStats(
        pendingInDb = dao.countByStatus(LogEvent.STATUS_PENDING),
        uploadingInDb = dao.countByStatus(LogEvent.STATUS_UPLOADING),
        deadInDb = dao.countByStatus(LogEvent.STATUS_DEAD),
        totalInDb = dao.totalCount(),
    )

    internal fun shutdown() {
        foregroundUploadJob?.cancel()
        scope.cancel()
    }

    private suspend fun recoverStaleUploads() {
        dao.resetStaleUploading(
            staleBefore = System.currentTimeMillis() - config.staleUploadingMs,
        )
    }

    private fun startBatchProcessor() {
        scope.launch {
            val buffer = ArrayList<LogEvent>(config.dbBatchSize)
            while (isActive) {
                buffer.add(channel.receive())
                val deadline = SystemClock.elapsedRealtime() + config.dbBatchTimeoutMs
                while (buffer.size < config.dbBatchSize) {
                    val remaining = deadline - SystemClock.elapsedRealtime()
                    if (remaining <= 0) break
                    val next = withTimeoutOrNull(remaining) { channel.receive() } ?: break
                    buffer.add(next)
                }
                flushEventsToDb(buffer.toList())
                buffer.clear()
            }
        }
    }

    private suspend fun drainChannelToDb() {
        val buffer = ArrayList<LogEvent>(config.dbBatchSize)
        while (true) {
            val event = channel.tryReceive().getOrNull() ?: break
            buffer.add(event)
            if (buffer.size >= config.dbBatchSize) {
                flushEventsToDb(buffer.toList())
                buffer.clear()
            }
        }
        if (buffer.isNotEmpty()) {
            flushEventsToDb(buffer)
        }
    }

    private suspend fun flushEventsToDb(events: List<LogEvent>) {
        if (events.isEmpty()) return
        runCatching {
            dao.insertAll(events)
            maybeTriggerUpload()
            enforceRetention()
        }
    }

    private suspend fun maybeTriggerUpload() {
        if (dao.countByStatus(LogEvent.STATUS_PENDING) >= config.uploadBatchSize) {
            UploadScheduler.enqueueOneTime(appContext, config)
        }
    }

    private suspend fun enforceRetention() {
        val cutoff = System.currentTimeMillis() - config.maxEventAgeMs
        dao.evictOld(cutoff)

        val total = dao.totalCount()
        if (total > config.maxDbEvents) {
            dao.evictExcess(total - config.maxDbEvents)
        }
    }

    private fun observeAppLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                UploadScheduler.enqueueOneTime(appContext, config)
                startForegroundUploadLoop()
            }

            override fun onStop(owner: LifecycleOwner) {
                stopForegroundUploadLoop()
                scope.launch { flush() }
            }
        })
    }

    private fun startForegroundUploadLoop() {
        if (config.uploadIntervalWhileForegroundMs <= 0) return
        foregroundUploadJob?.cancel()
        foregroundUploadJob = scope.launch {
            while (isActive) {
                delay(config.uploadIntervalWhileForegroundMs)
                if (dao.countByStatus(LogEvent.STATUS_PENDING) > 0) {
                    UploadScheduler.enqueueOneTime(appContext, config)
                }
            }
        }
    }

    private fun stopForegroundUploadLoop() {
        foregroundUploadJob?.cancel()
        foregroundUploadJob = null
    }

    private fun registerTrimMemoryCallback() {
        appContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
                    scope.launch { drainChannelToDb() }
                }
            }

            override fun onConfigurationChanged(newConfig: android.content.res.Configuration) = Unit

            override fun onLowMemory() {
                scope.launch { drainChannelToDb() }
            }
        })
    }
}
