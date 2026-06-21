package com.example.interview.eventloggerlab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventlogger.api.EventLogger
import com.example.eventlogger.api.LoggerStats
import com.example.eventlogger.model.LogEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EventLoggerLabViewModel @Inject constructor(
    private val eventLogger: EventLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventLoggerLabUiState())
    val uiState: StateFlow<EventLoggerLabUiState> = _uiState.asStateFlow()

    init {
        refreshStats()
    }

    fun logSingleEvent() {
        eventLogger.log(
            type = "button_click",
            properties = mapOf("screen" to "event_logger_lab"),
        )
        appendMessage("Logged 1 normal event")
        refreshStats()
    }

    fun logHighPriorityEvent() {
        eventLogger.log(
            type = "purchase_started",
            properties = mapOf("sku" to "premium_monthly"),
            priority = LogEvent.PRIORITY_HIGH,
        )
        appendMessage("Logged 1 HIGH priority event")
        refreshStats()
    }

    fun logCriticalEvent() {
        eventLogger.log(
            type = "payment_confirmed",
            properties = mapOf("order_id" to "ord_${System.currentTimeMillis()}"),
            priority = LogEvent.PRIORITY_CRITICAL,
        )
        appendMessage("Logged 1 CRITICAL event (direct to Room)")
        refreshStats()
    }

    fun logBurst(count: Int = 50) {
        repeat(count) { index ->
            eventLogger.log(
                type = "scroll_telemetry",
                properties = mapOf(
                    "index" to index,
                    "offset" to index * 24,
                ),
            )
        }
        appendMessage("Logged burst of $count events")
        refreshStats()
    }

    fun flush() {
        viewModelScope.launch {
            eventLogger.flush()
            appendMessage("Flush requested (channel drained + upload enqueued)")
            refreshStats()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val stats = eventLogger.stats()
            _uiState.update { it.copy(stats = stats) }
        }
    }

    private fun appendMessage(message: String) {
        _uiState.update { state ->
            val updated = (listOf(message) + state.messages).take(8)
            state.copy(messages = updated)
        }
    }
}

data class EventLoggerLabUiState(
    val stats: LoggerStats = LoggerStats(0, 0, 0, 0),
    val messages: List<String> = emptyList(),
)
