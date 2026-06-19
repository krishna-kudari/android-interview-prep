package com.example.interview.stopwatch.data

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.interview.stopwatch.domain.Clock
import com.example.interview.stopwatch.domain.StopwatchState
import kotlin.math.abs
import kotlinx.coroutines.flow.first

private val Context.stopwatchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stopwatch_prefs"
)

/**
 * Persists stopwatch state across process death using Preferences DataStore.
 *
 * WHY DATASTORE OVER SHAREDPREFERENCES
 * ────────────────────────────────────
 * • Atomic writes via temp file + rename — no partial file on crash mid-write.
 * • All I/O on Dispatchers.IO — no synchronous XML parse on first access.
 * • Native coroutine/Flow integration — save() fits naturally in the service collector.
 *
 * WHY WE NEED A BOOT FINGERPRINT
 * ────────────────────────────────
 * SystemClock.elapsedRealtime() resets to 0 on every device reboot. If the user reboots
 * while the stopwatch is running, the saved anchor becomes meaningless — on the next app
 * launch, (elapsedRealtime() − oldAnchor) could be negative or astronomically large.
 *
 * Fix: on every save we also record a "boot fingerprint":
 *   bootFingerprint = System.currentTimeMillis() − SystemClock.elapsedRealtime()
 *
 * This value is roughly constant for the lifetime of a single boot session (it is the
 * boot epoch in UTC milliseconds). After a reboot it shifts by the reboot duration, so
 * a large delta flags an anchor that can no longer be trusted.
 *
 * DEGRADATION POLICY ON REBOOT-DETECTED
 * ───────────────────────────────────────
 * We do NOT discard the user's accumulated progress. We convert Running → Paused so the
 * user sees how long the stopwatch ran before the reboot, not a corrupted value.
 * The anchor is lost, but accumulatedMillis is safe (it was saved, not recomputed).
 *
 * WRITE STRATEGY
 * ──────────────
 * We write on state TRANSITIONS only (start, pause, reset), never on every tick.
 * This avoids an I/O storm that would otherwise hit the main thread every 16 ms.
 */
class StopwatchPersistence(private val context: Context) {

    private object Keys {
        val TYPE = stringPreferencesKey("type")
        val ACCUMULATED = longPreferencesKey("accumulated")
        val ANCHOR = longPreferencesKey("anchor")
        val BOOT_FP = longPreferencesKey("boot_fp")
    }

    suspend fun save(state: StopwatchState) {
        val (type, accumulated, anchor) = when (state) {
            is StopwatchState.Idle -> Triple(TYPE_IDLE, 0L, 0L)
            is StopwatchState.Paused -> Triple(TYPE_PAUSED, state.accumulatedMillis, 0L)
            is StopwatchState.Running -> Triple(TYPE_RUNNING, state.accumulatedMillis, state.anchorRealtime)
        }
        context.stopwatchDataStore.edit { prefs ->
            prefs[Keys.TYPE] = type
            prefs[Keys.ACCUMULATED] = accumulated
            prefs[Keys.ANCHOR] = anchor
            prefs[Keys.BOOT_FP] = bootFingerprintNow()
        }
    }

    suspend fun restore(clock: Clock): StopwatchState {
        val prefs = context.stopwatchDataStore.data.first()
        val type = prefs[Keys.TYPE] ?: TYPE_IDLE
        val accumulated = prefs[Keys.ACCUMULATED] ?: 0L
        val anchor = prefs[Keys.ANCHOR] ?: 0L
        val rebooted = abs(bootFingerprintNow() - (prefs[Keys.BOOT_FP] ?: 0L)) > REBOOT_TOLERANCE_MS

        return when {
            type == TYPE_RUNNING && !rebooted ->
                StopwatchState.Running(anchor, accumulated)

            // Anchor is untrustworthy after reboot — freeze at last known progress,
            // don't lose it. The user can resume manually.
            type == TYPE_RUNNING && rebooted ->
                StopwatchState.Paused(accumulated)

            type == TYPE_PAUSED ->
                StopwatchState.Paused(accumulated)

            else -> StopwatchState.Idle
        }
    }

    suspend fun clear() {
        context.stopwatchDataStore.edit { it.clear() }
    }

    /**
     * Roughly the UTC epoch of the last boot in milliseconds.
     * Stable within a boot session; jumps sharply on reboot.
     */
    private fun bootFingerprintNow(): Long =
        System.currentTimeMillis() - SystemClock.elapsedRealtime()

    companion object {
        private const val TYPE_IDLE = "IDLE"
        private const val TYPE_RUNNING = "RUNNING"
        private const val TYPE_PAUSED = "PAUSED"

        // 5-second tolerance window to absorb minor NTP-induced wall-clock jitter
        // that would otherwise false-positive as a reboot.
        private const val REBOOT_TOLERANCE_MS = 5_000L
    }
}
