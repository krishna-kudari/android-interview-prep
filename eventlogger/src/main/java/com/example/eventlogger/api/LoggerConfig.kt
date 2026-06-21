package com.example.eventlogger.api

data class LoggerConfig(
    val memoryQueueCapacity: Int = 256,
    val dbBatchSize: Int = 50,
    val dbBatchTimeoutMs: Long = 5_000L,
    val uploadBatchSize: Int = 100,
    val uploadPeriodicMin: Long = 15L,
    val uploadIntervalWhileForegroundMs: Long = 30_000L,
    val maxDbEvents: Int = 10_000,
    val maxEventAgeMs: Long = 7L * 24 * 3_600 * 1_000,
    val maxUploadRetries: Int = 5,
    val staleUploadingMs: Long = 5L * 60 * 1_000,
    val requireUnmetered: Boolean = false,
    val uploader: EventUploader,
) {
    init {
        require(memoryQueueCapacity > 0)
        require(dbBatchSize > 0)
        require(dbBatchTimeoutMs > 0)
        require(uploadBatchSize > 0)
        require(uploadPeriodicMin >= 15) { "WorkManager periodic minimum is 15 minutes" }
        require(maxDbEvents > 0)
        require(maxUploadRetries >= 0)
    }

    class Builder(private var uploader: EventUploader? = null) {
        private var memoryQueueCapacity = 256
        private var dbBatchSize = 50
        private var dbBatchTimeoutMs = 5_000L
        private var uploadBatchSize = 100
        private var uploadPeriodicMin = 15L
        private var uploadIntervalWhileForegroundMs = 30_000L
        private var maxDbEvents = 10_000
        private var maxEventAgeMs = 7L * 24 * 3_600 * 1_000
        private var maxUploadRetries = 5
        private var staleUploadingMs = 5L * 60 * 1_000
        private var requireUnmetered = false

        fun uploader(uploader: EventUploader) = apply { this.uploader = uploader }

        fun memoryQueueCapacity(value: Int) = apply { memoryQueueCapacity = value }

        fun dbBatchSize(value: Int) = apply { dbBatchSize = value }

        fun dbBatchTimeoutMs(value: Long) = apply { dbBatchTimeoutMs = value }

        fun uploadBatchSize(value: Int) = apply { uploadBatchSize = value }

        fun uploadPeriodicMin(value: Long) = apply { uploadPeriodicMin = value }

        fun uploadIntervalWhileForegroundMs(value: Long) = apply {
            uploadIntervalWhileForegroundMs = value
        }

        fun maxDbEvents(value: Int) = apply { maxDbEvents = value }

        fun maxEventAgeMs(value: Long) = apply { maxEventAgeMs = value }

        fun maxUploadRetries(value: Int) = apply { maxUploadRetries = value }

        fun staleUploadingMs(value: Long) = apply { staleUploadingMs = value }

        fun requireUnmetered(value: Boolean) = apply { requireUnmetered = value }

        fun build(): LoggerConfig = LoggerConfig(
            memoryQueueCapacity = memoryQueueCapacity,
            dbBatchSize = dbBatchSize,
            dbBatchTimeoutMs = dbBatchTimeoutMs,
            uploadBatchSize = uploadBatchSize,
            uploadPeriodicMin = uploadPeriodicMin,
            uploadIntervalWhileForegroundMs = uploadIntervalWhileForegroundMs,
            maxDbEvents = maxDbEvents,
            maxEventAgeMs = maxEventAgeMs,
            maxUploadRetries = maxUploadRetries,
            staleUploadingMs = staleUploadingMs,
            requireUnmetered = requireUnmetered,
            uploader = checkNotNull(uploader) { "EventUploader is required" },
        )
    }
}
