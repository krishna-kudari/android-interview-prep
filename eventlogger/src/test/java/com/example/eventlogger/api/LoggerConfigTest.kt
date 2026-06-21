package com.example.eventlogger.api

import com.example.eventlogger.model.LogEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggerConfigTest {

    private val noopUploader = EventUploader { UploadResult.Success }

    @Test
    fun builder_appliesDefaults() {
        val config = LoggerConfig.Builder(uploader = noopUploader).build()

        assertEquals(256, config.memoryQueueCapacity)
        assertEquals(50, config.dbBatchSize)
        assertEquals(100, config.uploadBatchSize)
    }

    @Test(expected = IllegalArgumentException::class)
    fun builder_rejectsPeriodicBelowWorkManagerMinimum() {
        LoggerConfig.Builder(uploader = noopUploader)
            .uploadPeriodicMin(5)
            .build()
    }

    @Test
    fun criticalPriority_isHighest() {
        assertTrue(LogEvent.PRIORITY_CRITICAL > LogEvent.PRIORITY_HIGH)
    }
}
