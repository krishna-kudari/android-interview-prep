package com.example.interview.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Small sandbox for comparing [StateFlow] vs [LiveData] in Compose and for Nav / VM scoping labs.
 */
class ComposeLabViewModel : ViewModel() {

    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _liveLabel = MutableLiveData("LiveData: tap Refresh")
    val liveLabel: LiveData<String> = _liveLabel

    fun incrementCounter() {
        _counter.update { it + 1 }
    }

    fun refreshLiveLabel() {
        _liveLabel.value = "LiveData: ${System.currentTimeMillis() % 100_000}"
    }
}
