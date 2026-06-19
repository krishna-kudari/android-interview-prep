package com.example.interview.stopwatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interview.stopwatch.domain.StopwatchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.interview.stopwatch.repository.StopwatchRepository
import com.example.interview.stopwatch.util.formatMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * What the UI needs to render — no domain types leak into the Composable.
 */
data class StopwatchUiState(
    val displayTime: String = "00:00.00",
    val isRunning: Boolean = false,
    val isIdle: Boolean = true
)

/**
 * Owns the display ticker and translates domain state to a single [StopwatchUiState].
 *
 * TICK STRATEGY
 * ─────────────
 * The ticker (~60 fps) runs ONLY while the engine is in Running state. When paused
 * or idle the flow emits a single snapshot value and stops. This avoids wasting CPU
 * redrawing a static display.
 *
 * Ticking never mutates engine state — it only triggers a re-read of [elapsedMillis],
 * which is a pure function. Jitter in the 16ms dispatch interval has zero cumulative
 * effect because elapsed time is always recomputed from the anchor on every read.
 *
 * SHARING & LIFECYCLE
 * ────────────────────
 * stateIn with WhileSubscribed(5_000):
 * • Keeps the upstream active for 5s after the last subscriber drops.
 * • A rotation finishes in ~300ms, so the coroutine is never cancelled during rotation.
 * • After 5s with no subscribers (app truly backgrounded) the upstream is cancelled.
 *   The service is still running (started, not just bound) so no state is lost.
 *
 * BIND/UNBIND — NOT IN VIEWMODEL
 * ───────────────────────────────
 * bind() / unbind() are called from StopwatchActivity.onStart/onStop, NOT from here.
 * ServiceConnection holds an Activity Context reference and must not outlive the
 * Activity. The ViewModel survives rotation but the binding should not.
 */
class StopwatchViewModel(
    private val repository: StopwatchRepository
) : ViewModel() {

    // ~60fps ticker — only active while Running; produces Unit to trigger a re-read.
    private val uiTicker = flow {
        while (true) {
            emit(Unit)
            kotlinx.coroutines.delay(16L)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StopwatchUiState> = repository.state
        .flatMapLatest { state ->
            when (state) {
                is StopwatchState.Running ->
                    uiTicker.map {
                        StopwatchUiState(
                            displayTime = formatMillis(repository.elapsedMillis()),
                            isRunning = true,
                            isIdle = false
                        )
                    }

                is StopwatchState.Paused ->
                    flowOf(
                        StopwatchUiState(
                            displayTime = formatMillis(state.accumulatedMillis),
                            isRunning = false,
                            isIdle = false
                        )
                    )

                is StopwatchState.Idle ->
                    flowOf(StopwatchUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StopwatchUiState()
        )

    fun onStartPauseClicked() {
        if (uiState.value.isRunning) repository.pause() else repository.start()
    }

    fun onResetClicked() = repository.reset()

    override fun onCleared() {
        super.onCleared()
        // Repository unbind is handled by the Activity to ensure it uses the correct
        // Context lifetime. This is a safety net for the extremely rare case the
        // ViewModel is cleared without onStop firing (process death).
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Manual factory — no Hilt, consistent with "no libraries" constraint.
    // ──────────────────────────────────────────────────────────────────────────────

    class Factory(private val repository: StopwatchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == StopwatchViewModel::class.java) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return StopwatchViewModel(repository) as T
        }
    }
}
