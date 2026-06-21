package com.example.eventlogger.upload

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.eventlogger.api.LoggerConfig
import java.util.concurrent.TimeUnit

object UploadScheduler {

    private const val PERIODIC = "event_logger_upload_periodic"
    private const val ONE_TIME = "event_logger_upload_one_time"

    fun schedulePeriodic(context: Context, config: LoggerConfig) {
        val request = PeriodicWorkRequestBuilder<UploadWorker>(
            config.uploadPeriodicMin,
            TimeUnit.MINUTES,
        )
            .setConstraints(constraints(config))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueOneTime(
        context: Context,
        config: LoggerConfig,
        replaceExisting: Boolean = false,
    ) {
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints(config))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME,
            if (replaceExisting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun constraints(config: LoggerConfig): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(
                if (config.requireUnmetered) NetworkType.UNMETERED else NetworkType.CONNECTED,
            )
            .build()
}
