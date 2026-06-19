package com.example.interview.stopwatch.domain

/**
 * Explicit state machine replaces boolean flag soup (isRunning, isPaused, isReset…).
 * Only valid transitions exist; impossible states are unrepresentable at compile time.
 *
 *            start()                 pause()
 *   ┌──────┐ ───────▶ ┌─────────┐ ───────▶ ┌────────┐
 *   │ Idle │          │ Running │          │ Paused │
 *   └──────┘ ◀─────── └─────────┘ ◀─────── └────────┘
 *      ▲    reset()        ▲    start()/resume()  │
 *      └──────────────────────────────────────────reset()
 *
 * Start and Resume are the same transition — both enter Running with a fresh anchor.
 * This collapses an isFirstStart flag that would otherwise be a third boolean.
 */
sealed class StopwatchState {
    data object Idle : StopwatchState()

    /**
     * @param anchorRealtime   elapsedRealtime() value captured when the last resume happened.
     * @param accumulatedMillis milliseconds already elapsed in previous running intervals.
     */
    data class Running(
        val anchorRealtime: Long,
        val accumulatedMillis: Long
    ) : StopwatchState()

    /**
     * @param accumulatedMillis total elapsed milliseconds at the moment of pause.
     */
    data class Paused(val accumulatedMillis: Long) : StopwatchState()
}
