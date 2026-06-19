package com.example.interview.stopwatch.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.interview.stopwatch.domain.StopwatchEngine
import com.example.interview.stopwatch.domain.StopwatchState
import com.example.interview.stopwatch.service.StopwatchForegroundService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Bridges the UI layer and the foreground service.
 *
 * BIND/UNBIND LIFECYCLE
 * ──────────────────────
 * bind() must be called from the Activity's onStart() — NOT from ViewModel.init{}.
 * The ViewModel survives configuration changes, but the Context used for binding is
 * Activity-scoped. If we bound in ViewModel.init we would leak the Activity Context
 * across rotations via the ServiceConnection callback.
 *
 * unbind() goes in onStop() (not onDestroy()) so the system can destroy the Activity
 * process during background without blocking the service, and so we don't hold the
 * connection across the rotation gap longer than necessary.
 *
 * The service itself was started via startForegroundService() before bindService(), so
 * it outlives this connection. During the rotation gap (brief period with 0 bound
 * clients) the started component keeps it alive.
 *
 * FLOW SWITCHMAP
 * ──────────────
 * [state] uses flatMapLatest on [_connected] so that:
 * • While connected: it streams the live engine StateFlow.
 * • While not connected (brief rotation gap): it emits Idle to avoid stale state
 *   leaking through. The ViewModel's stateIn with WhileSubscribed(5000) means a
 *   brief disconnection won't trigger a cold restart of the upstream — it waits 5s
 *   before cancelling the coroutine, which is always longer than a rotation takes.
 */
class StopwatchRepository(private val context: Context) {

    private var engine: StopwatchEngine? = null
    private val _connected = MutableStateFlow(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            engine = (binder as StopwatchForegroundService.StopwatchBinder).getEngine()
            _connected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            engine = null
            _connected.value = false
        }
    }

    /**
     * Start + bind. Call from Activity.onStart().
     * startForegroundService ensures the service outlives any client unbind.
     */
    fun bind() {
        val intent = Intent(context, StopwatchForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    /** Call from Activity.onStop(). */
    fun unbind() {
        if (_connected.value) {
            context.unbindService(connection)
            _connected.value = false
            engine = null
        }
    }

    /**
     * Live state stream. When connected, mirrors the engine's StateFlow.
     * Emits Idle while the service connection is being established to
     * prevent the UI from rendering a stale snapshot.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<StopwatchState> = _connected.flatMapLatest { connected ->
        if (connected) engine?.state ?: flowOf(StopwatchState.Idle) else flowOf(StopwatchState.Idle)
    }

    fun start() = engine?.start()
    fun pause() = engine?.pause()
    fun reset() = engine?.reset()

    /** Snapshot read — safe to call from any coroutine context. */
    fun elapsedMillis(): Long = engine?.elapsedMillis() ?: 0L
}
