package com.example.interview.stopwatch.util

/**
 * Formats elapsed milliseconds into MM:SS.cc (centiseconds) display string.
 *
 * Examples:
 *   0       → "00:00.00"
 *   1_234   → "00:01.23"
 *   65_000  → "01:05.00"
 *   3600000 → "60:00.00"  (no hours digit — hours shows as overflow minutes,
 *                           consistent with classic stopwatch UX)
 */
fun formatMillis(ms: Long): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val centiseconds = (ms % 1_000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
}
