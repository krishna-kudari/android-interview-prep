package com.example.eventlogger.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "events",
    indices = [
        Index("status"),
        Index("priority"),
        Index("timestamp"),
    ],
)
data class LogEvent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = PRIORITY_NORMAL,
    val status: Int = STATUS_PENDING,
    val retryCount: Int = 0,
) {
    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_NORMAL = 1
        const val PRIORITY_HIGH = 2
        const val PRIORITY_CRITICAL = 3

        const val STATUS_PENDING = 0
        const val STATUS_UPLOADING = 1
        const val STATUS_DEAD = 2
    }
}
