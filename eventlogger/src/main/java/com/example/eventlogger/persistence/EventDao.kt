package com.example.eventlogger.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventlogger.model.LogEvent

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<LogEvent>)

    @Query(
        """
        SELECT * FROM events
        WHERE status = :status
        ORDER BY priority DESC, timestamp ASC
        LIMIT :limit
        """,
    )
    suspend fun getByStatus(
        status: Int,
        limit: Int,
    ): List<LogEvent>

    @Query("UPDATE events SET status = :status WHERE id IN (:ids)")
    suspend fun setStatus(ids: List<String>, status: Int)

    @Query("DELETE FROM events WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query(
        """
        UPDATE events
        SET status = :pendingStatus, retryCount = retryCount + 1
        WHERE id IN (:ids)
        """,
    )
    suspend fun markRetry(
        ids: List<String>,
        pendingStatus: Int = LogEvent.STATUS_PENDING,
    )

    @Query("SELECT COUNT(*) FROM events WHERE status = :status")
    suspend fun countByStatus(status: Int): Int

    @Query("SELECT COUNT(*) FROM events")
    suspend fun totalCount(): Int

    @Query(
        """
        DELETE FROM events
        WHERE timestamp < :cutoff AND priority < :minPriorityToKeep
        """,
    )
    suspend fun evictOld(
        cutoff: Long,
        minPriorityToKeep: Int = LogEvent.PRIORITY_HIGH,
    ): Int

    @Query(
        """
        DELETE FROM events WHERE id IN (
            SELECT id FROM events
            WHERE priority < :minPriorityToKeep
            ORDER BY timestamp ASC
            LIMIT :excessCount
        )
        """,
    )
    suspend fun evictExcess(
        excessCount: Int,
        minPriorityToKeep: Int = LogEvent.PRIORITY_HIGH,
    ): Int

    @Query(
        """
        UPDATE events
        SET status = :pendingStatus
        WHERE status = :uploadingStatus AND timestamp < :staleBefore
        """,
    )
    suspend fun resetStaleUploading(
        uploadingStatus: Int = LogEvent.STATUS_UPLOADING,
        pendingStatus: Int = LogEvent.STATUS_PENDING,
        staleBefore: Long,
    ): Int
}
