package com.example.interview.eventloggerlab

import com.example.eventlogger.api.EventUploader
import com.example.eventlogger.api.UploadResult
import com.example.eventlogger.model.LogEvent
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Example [EventUploader] that POSTs a JSON array to any analytics endpoint.
 * Swap [TimberEventUploader] with this in [di.EventLoggerModule] when wiring a real backend.
 */
class HttpEventUploader(
    private val endpointUrl: String,
    private val client: OkHttpClient = defaultClient(),
    moshi: Moshi = Moshi.Builder().build(),
) : EventUploader {

    private val adapter = moshi.adapter<List<EventPayload>>(
        Types.newParameterizedType(List::class.java, EventPayload::class.java),
    )

    override suspend fun upload(events: List<LogEvent>): UploadResult {
        val body = adapter.toJson(events.map { it.toPayload() })
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url(endpointUrl)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> UploadResult.Success
                    response.code in 400..499 -> UploadResult.Permanent("HTTP ${response.code}")
                    else -> UploadResult.Retryable("HTTP ${response.code}")
                }
            }
        } catch (e: IOException) {
            UploadResult.Retryable(e.message ?: "network error")
        }
    }

    private fun LogEvent.toPayload() = EventPayload(
        id = id,
        type = type,
        payload = payload,
        timestamp = timestamp,
        priority = priority,
    )

    @JsonClass(generateAdapter = true)
    data class EventPayload(
        val id: String,
        val type: String,
        val payload: String,
        val timestamp: Long,
        val priority: Int,
    )

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()

        fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
    }
}
