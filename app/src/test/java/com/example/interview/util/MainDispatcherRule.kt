package com.example.interview.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestWatcher] that replaces [Dispatchers.Main] with a [TestDispatcher] for the
 * duration of each test, then restores it on teardown.
 *
 * By passing the same [testDispatcher] to both this rule and [kotlinx.coroutines.test.runTest],
 * all coroutines (including [androidx.lifecycle.viewModelScope]) share one scheduler, giving
 * deterministic, time-controlled execution without real delays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
