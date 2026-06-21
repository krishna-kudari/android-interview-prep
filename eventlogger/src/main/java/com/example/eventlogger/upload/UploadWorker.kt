package com.example.eventlogger.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.eventlogger.api.UploadResult
import com.example.eventlogger.internal.LoggerHolder
import com.example.eventlogger.model.LogEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val (dao, config) = LoggerHolder.require()

        try {
            dao.resetStaleUploading(
                staleBefore = System.currentTimeMillis() - config.staleUploadingMs,
            )

            while (true) {
                val batch = dao.getByStatus(LogEvent.STATUS_PENDING, config.uploadBatchSize)
                if (batch.isEmpty()) break

                val ids = batch.map { it.id }
                dao.setStatus(ids, LogEvent.STATUS_UPLOADING)

                when (val result = config.uploader.upload(batch)) {
                    is UploadResult.Success -> dao.delete(ids)

                    is UploadResult.Retryable -> {
                        dao.markRetry(ids)
                        return@withContext if (runAttemptCount < config.maxUploadRetries) {
                            Result.retry()
                        } else {
                            Result.failure()
                        }
                    }

                    is UploadResult.Permanent -> {
                        dao.setStatus(ids, LogEvent.STATUS_DEAD)
                    }
                }
            }

            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
