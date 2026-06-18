package com.example.interview.coroutineslab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoroutinesPlaygroundViewModel @Inject constructor() : ViewModel() {

    private val _selectedProblem = MutableStateFlow<CoroutineProblem?>(null)
    val selectedProblem = _selectedProblem.asStateFlow()

    private val _output = MutableStateFlow<List<OutputLine>>(emptyList())
    val output = _output.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private var runJob: Job? = null
    private var startTimeMs = 0L

    fun selectProblem(problem: CoroutineProblem) {
        _selectedProblem.value = problem
        _output.value = emptyList()
        stopProblem()
    }

    fun clearSelection() {
        stopProblem()
        _selectedProblem.value = null
        _output.value = emptyList()
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun runProblem() {
        val problem = _selectedProblem.value ?: return
        stopProblem()
        _output.value = emptyList()
        _isRunning.value = true
        startTimeMs = System.currentTimeMillis()

        runJob = viewModelScope.launch {
            appendLine("▶ Running: ${problem.title}")
            appendLine("─".repeat(40))
            try {
                coroutineScope {
                    problem.runner(this) { message ->
                        val elapsed = System.currentTimeMillis() - startTimeMs
                        appendLine("+${elapsed}ms  $message")
                    }
                }
                appendLine("─".repeat(40))
                val total = System.currentTimeMillis() - startTimeMs
                appendLine("✓ Completed in ${total}ms")
            } catch (e: CancellationException) {
                appendLine("─".repeat(40))
                appendLine("⏹ Stopped by user")
            } catch (e: Exception) {
                appendLine("─".repeat(40))
                appendLine("✗ ${e::class.simpleName}: ${e.message}", isError = true)
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun stopProblem() {
        runJob?.cancel()
        runJob = null
    }

    fun clearOutput() {
        _output.value = emptyList()
    }

    private fun appendLine(text: String, isError: Boolean = false) {
        _output.value = _output.value + OutputLine(text, isError)
    }
}
