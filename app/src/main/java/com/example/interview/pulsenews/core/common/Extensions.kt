package com.example.interview.pulsenews.core.common

import java.time.Instant
import java.time.ZoneId

private val dateFormatter =
    java.time.format.DateTimeFormatter
        .ofPattern("MMM d")
        .withZone(ZoneId.systemDefault())

fun String.toRelativeTime(): String {
    return try {
        val published = Instant.parse(this)
        val now = Instant.now()

        val duration = java.time.Duration.between(published, now)

        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> dateFormatter.format(published)
        }
    } catch (e: Exception) {
        this
    }
}