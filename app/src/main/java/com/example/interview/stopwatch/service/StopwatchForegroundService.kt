package com.example.interview.stopwatch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.interview.R
import com.example.interview.stopwatch.StopwatchActivity
import com.example.interview.stopwatch.data.StopwatchPersistence
import com.example.interview.stopwatch.domain.RealClock
import com.example.interview.stopwatch.domain.StopwatchEngine
import com.example.interview.stopwatch.domain.StopwatchState
import com.example.interview.stopwatch.util.formatMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Hybrid started-and-bound foreground service.
 *
 * WHY HYBRID (not purely bound, not purely started)?
 * ──────────────────────────────────────────────────
 * • Bound-only: dies the instant the last client unbinds. During Activity recreation
 *   (rotation), there is a brief window with zero clients — the service would stop and
 *   lose state.
 * • Started-only: survives unbind, but the caller gets no direct in-process interface.
 *   We'd need Messenger/AIDL for same-process communication — unnecessary overhead.
 * • Hybrid: startForegroundService() keeps it alive across unbind gaps; bindService()
 *   gives the Repository a direct Binder reference with no IPC overhead.
 *
 * FOREGROUND SERVICE TYPE — WHY specialUse?
 * ──────────────────────────────────────────
 * Android 14 (API 34) requires every FGS to declare a foregroundServiceType. A stopwatch
 * doesn't fit location, camera, mediaPlayback, etc. The specialUse type (added in API 34)
 * covers this. Declaring dataSync instead is tempting but wrong: as of apps targeting
 * API 35, dataSync FGS has runtime duration limits that will silently kill a long-running
 * stopwatch. specialUse requires a <property> descriptor reviewed by Google Play.
 *
 * TICK RATE
 * ─────────
 * The notification ticker runs at 1 Hz — notifications only display second-level
 * precision, so faster updates would waste battery with zero user benefit. The UI
 * ticker (in ViewModel) runs at ~60 fps independently via a separate coroutine.
 */
class StopwatchForegroundService : Service() {

    private val binder = StopwatchBinder()
    lateinit var engine: StopwatchEngine
        private set

    private lateinit var persistence: StopwatchPersistence
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null

    inner class StopwatchBinder : Binder() {
        fun getEngine(): StopwatchEngine = engine
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val clock = RealClock()
        persistence = StopwatchPersistence(applicationContext)
        // One acceptable runBlocking: single read of ~4 keys, once at service creation,
        // before startForeground() — we can't show a notification for unloaded state.
        val initialState = runBlocking { persistence.restore(clock) }
        engine = StopwatchEngine(clock, initialState)

        startForeground(NOTIFICATION_ID, buildNotification(engine.elapsedMillis()))

        // Persist on every state transition (not every tick — no I/O storm).
        serviceScope.launch {
            engine.state.collect { state ->
                persistence.save(state)
                manageNotificationTicker(state)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    /**
     * Called when the last client unbinds. Return true so the service is not destroyed
     * when all clients unbind — the started component already handles lifetime.
     */
    override fun onUnbind(intent: Intent?): Boolean = true

    override fun onDestroy() {
        runBlocking { persistence.save(engine.state.value) } // final flush before process exits
        serviceScope.cancel()
        super.onDestroy()
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Notification ticker
    // ──────────────────────────────────────────────────────────────────────────────

    private fun manageNotificationTicker(state: StopwatchState) {
        tickerJob?.cancel()
        if (state is StopwatchState.Running) {
            tickerJob = serviceScope.launch {
                while (isActive) {
                    updateNotification(engine.elapsedMillis())
                    delay(NOTIFICATION_TICK_MS)
                }
            }
        } else {
            updateNotification(engine.elapsedMillis())
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Notification helpers
    // ──────────────────────────────────────────────────────────────────────────────

    private fun buildNotification(elapsed: Long): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, StopwatchActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stopwatch")
            .setContentText(formatMillis(elapsed))
            .setSmallIcon(R.drawable.ic_stopwatch)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(tapIntent)
            .build()
    }

    private fun updateNotification(elapsed: Long) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(elapsed))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Stopwatch",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows elapsed stopwatch time while running in the background"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "stopwatch_channel"
        private const val NOTIFICATION_TICK_MS = 1_000L
    }
}
