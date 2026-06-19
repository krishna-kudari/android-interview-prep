package com.example.interview.stopwatch.domain

import android.os.SystemClock

/**
 * Abstraction over the time source so the engine is testable without real clocks.
 *
 * We use elapsedRealtime() — monotonic, continues across deep sleep, unaffected by
 * wall-clock mutations (NTP, manual date change). It resets only on device reboot,
 * which the persistence layer handles explicitly via a boot fingerprint.
 */
interface Clock {
    fun elapsedRealtime(): Long
}

class RealClock : Clock {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
}
