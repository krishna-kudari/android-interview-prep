package com.example.interview.stopwatch.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pure domain object — zero Android framework dependencies, fully unit-testable.
 *
 * Key insight: ticking NEVER mutates state. [elapsedMillis] is a pure read computed
 * from anchors, so tick-dispatch jitter never compounds into drift. After an hour the
 * error is bounded by a single call overhead, not accumulated over thousands of ticks.
 *
 *   elapsed = accumulatedBeforePause + (clock.now() − anchorAtLastResume)
 *
 * [clock] is injected so tests can drive time deterministically with a FakeClock.
 */
class StopwatchEngine(
    private val clock: Clock,
    initialState: StopwatchState = StopwatchState.Idle
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    /** Start (first time) or Resume (after pause) — same transition, different baseline. */
    fun start() {
        val current = _state.value
        if (current is StopwatchState.Running) return // idempotent guard
        val accumulated = (current as? StopwatchState.Paused)?.accumulatedMillis ?: 0L
        _state.value = StopwatchState.Running(
            anchorRealtime = clock.elapsedRealtime(),
            accumulatedMillis = accumulated
        )
    }

    fun pause() {
        val current = _state.value as? StopwatchState.Running ?: return
        val elapsed = current.accumulatedMillis + (clock.elapsedRealtime() - current.anchorRealtime)
        _state.value = StopwatchState.Paused(elapsed)
    }

    fun reset() {
        _state.value = StopwatchState.Idle
    }

    /**
     * Pure read — safe to call every frame from any thread.
     * Never writes to [_state], so it cannot introduce drift.
     */
    fun elapsedMillis(now: Long = clock.elapsedRealtime()): Long =
        when (val s = _state.value) {
            is StopwatchState.Idle -> 0L
            is StopwatchState.Paused -> s.accumulatedMillis
            is StopwatchState.Running -> s.accumulatedMillis + (now - s.anchorRealtime)
        }
}
