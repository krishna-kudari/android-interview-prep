package com.example.interview.stopwatch

import com.example.interview.stopwatch.domain.Clock
import com.example.interview.stopwatch.domain.StopwatchEngine
import com.example.interview.stopwatch.domain.StopwatchState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [StopwatchEngine].
 *
 * These tests are:
 * • Pure JVM — no Android framework, no Robolectric, no real time.
 * • Deterministic — clock is fully controlled via [FakeClock].
 * • Fast — each test completes in < 1 ms; no Thread.sleep anywhere.
 *
 * This is exactly the value of injecting [Clock] instead of calling
 * SystemClock.elapsedRealtime() directly.
 */
class StopwatchEngineTest {

    // ──────────────────────────────────────────────────────────────────────────────
    // Test double
    // ──────────────────────────────────────────────────────────────────────────────

    /**
     * Fake clock whose time advances only when the test says so.
     * No thread scheduling, no dispatch overhead — drift is structurally impossible.
     */
    private class FakeClock(var time: Long = 0L) : Clock {
        override fun elapsedRealtime() = time
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Initial state
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `initial state is Idle`() {
        val engine = StopwatchEngine(FakeClock())
        assertTrue(engine.state.value is StopwatchState.Idle)
        assertEquals(0L, engine.elapsedMillis())
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Start
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `start transitions from Idle to Running`() {
        val clock = FakeClock(1_000)
        val engine = StopwatchEngine(clock)
        engine.start()
        val state = engine.state.value
        assertTrue(state is StopwatchState.Running)
        assertEquals(1_000L, (state as StopwatchState.Running).anchorRealtime)
        assertEquals(0L, state.accumulatedMillis)
    }

    @Test
    fun `start is idempotent when already Running`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)
        engine.start()
        val firstAnchor = (engine.state.value as StopwatchState.Running).anchorRealtime

        clock.time = 5_000
        engine.start() // should be a no-op

        val state = engine.state.value as StopwatchState.Running
        assertEquals(firstAnchor, state.anchorRealtime) // anchor unchanged
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Elapsed time — the drift-proof formula
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `elapsed grows correctly without mutation between reads`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()

        clock.time = 10_000
        assertEquals(10_000L, engine.elapsedMillis())

        clock.time = 15_000
        assertEquals(15_000L, engine.elapsedMillis())

        // State object itself must not have changed — only elapsedMillis() is recomputed.
        val state = engine.state.value as StopwatchState.Running
        assertEquals(0L, state.anchorRealtime)     // anchor is still 0
        assertEquals(0L, state.accumulatedMillis)  // accumulated still 0
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Pause
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `pause captures elapsed and transitions to Paused`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()
        clock.time = 7_000
        engine.pause()

        val state = engine.state.value
        assertTrue(state is StopwatchState.Paused)
        assertEquals(7_000L, (state as StopwatchState.Paused).accumulatedMillis)
        assertEquals(7_000L, engine.elapsedMillis())
    }

    @Test
    fun `pause is a no-op when not Running`() {
        val engine = StopwatchEngine(FakeClock())
        engine.pause() // from Idle — must not crash or change state
        assertTrue(engine.state.value is StopwatchState.Idle)
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // THE KEY TEST — time elapsed while paused is not counted
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `pause then resume ignores time elapsed while paused`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()            // anchor = 0
        clock.time = 5_000
        engine.pause()            // accumulated = 5000

        clock.time = 9_000        // 4 seconds pass while paused — must be invisible
        engine.start()            // resume: anchor = 9000, accumulated = 5000

        clock.time = 12_000       // 3 more seconds of running
        // Expected: 5000 (pre-pause) + (12000 - 9000) = 8000
        assertEquals(8_000L, engine.elapsedMillis())
    }

    @Test
    fun `multiple pause-resume cycles accumulate correctly`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        // Run 1: 3s
        engine.start()
        clock.time = 3_000
        engine.pause()

        // Paused 2s (invisible)
        clock.time = 5_000
        engine.start()

        // Run 2: 4s
        clock.time = 9_000
        engine.pause()

        // Total expected: 3000 + 4000 = 7000
        assertEquals(7_000L, engine.elapsedMillis())

        // Paused 10s (invisible)
        clock.time = 19_000
        engine.start()

        // Run 3: 1s
        clock.time = 20_000
        // Total expected: 7000 + 1000 = 8000
        assertEquals(8_000L, engine.elapsedMillis())
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Reset
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `reset from Running returns to Idle with 0 elapsed`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()
        clock.time = 5_000
        engine.reset()

        assertTrue(engine.state.value is StopwatchState.Idle)
        assertEquals(0L, engine.elapsedMillis())
    }

    @Test
    fun `reset from Paused returns to Idle with 0 elapsed`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()
        clock.time = 3_000
        engine.pause()
        engine.reset()

        assertTrue(engine.state.value is StopwatchState.Idle)
        assertEquals(0L, engine.elapsedMillis())
    }

    @Test
    fun `can start again after reset`() {
        val clock = FakeClock(0)
        val engine = StopwatchEngine(clock)

        engine.start()
        clock.time = 5_000
        engine.reset()

        // Start fresh — accumulated must not carry over from the previous run.
        clock.time = 10_000
        engine.start()
        clock.time = 12_000
        assertEquals(2_000L, engine.elapsedMillis())
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Restore from persisted Running state
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `engine restored from Running state computes elapsed correctly`() {
        val clock = FakeClock(20_000)
        // Simulate: engine was running since t=10000 with 5000ms already accumulated.
        val restored = StopwatchState.Running(anchorRealtime = 10_000, accumulatedMillis = 5_000)
        val engine = StopwatchEngine(clock, initialState = restored)

        // elapsed = 5000 + (20000 - 10000) = 15000
        assertEquals(15_000L, engine.elapsedMillis())
    }

    @Test
    fun `engine restored from Paused state returns accumulated millis unchanged`() {
        val clock = FakeClock(99_000)
        val restored = StopwatchState.Paused(accumulatedMillis = 42_000)
        val engine = StopwatchEngine(clock, initialState = restored)

        // Clock time must not affect a Paused state — it is frozen.
        assertEquals(42_000L, engine.elapsedMillis())
    }
}
