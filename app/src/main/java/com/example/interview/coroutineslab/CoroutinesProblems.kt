@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.interview.coroutineslab

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

val coroutineProblemCategories: List<ProblemCategory> = listOf(

    // ─── launch ───────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "launch",
        emoji = "🚀",
        problems = listOf(

            CoroutineProblem(
                id = "launch_nonblocking",
                title = "launch is non-blocking",
                category = "launch",
                description = "launch schedules a coroutine and returns immediately. " +
                        "Code after the launch call continues before the coroutine body runs.",
                hint = "Notice '2: After launch' prints BEFORE '3: Inside launch'",
                codeSnippet = """
log("1: Before launch")
val job = launch {
    log("3: Inside launch")
    delay(100)
    log("5: After delay inside launch")
}
log("2: After launch (runs immediately)")
delay(50)
log("4: Main after 50ms")
job.join()
log("6: After join")
""".trimIndent(),
                runner = { log ->
                    log("1: Before launch")
                    val job = launch {
                        log("3: Inside launch")
                        delay(100)
                        log("5: After delay inside launch")
                    }
                    log("2: After launch (runs immediately)")
                    delay(50)
                    log("4: Main after 50ms")
                    job.join()
                    log("6: After join")
                }
            ),

            CoroutineProblem(
                id = "launch_multiple",
                title = "Multiple launches — who runs first?",
                category = "launch",
                description = "Multiple launch blocks are scheduled concurrently. " +
                        "The one with the shortest delay finishes first.",
                hint = "C (no delay) should print first among the three, then D, then B, then A",
                codeSnippet = """
launch { log("A: start 300ms"); delay(300); log("A: done") }
launch { log("B: start 100ms"); delay(100); log("B: done") }
launch { log("C: no delay"); log("C: done") }
log("D: after all launches (synchronous)")
delay(400)
""".trimIndent(),
                runner = { log ->
                    launch { log("A: start 300ms"); delay(300); log("A: done") }
                    launch { log("B: start 100ms"); delay(100); log("B: done") }
                    launch { log("C: no delay"); log("C: done") }
                    log("D: after all launches (synchronous)")
                    delay(400)
                }
            ),

            CoroutineProblem(
                id = "launch_join",
                title = "join() — wait for a job",
                category = "launch",
                description = "job.join() suspends the caller until the job completes. " +
                        "Everything after join() is guaranteed to run after the job.",
                hint = "'After join' is always last",
                codeSnippet = """
log("Before launch")
val job = launch {
    delay(400)
    log("Job completed after 400ms")
}
log("After launch — job still running")
log("Calling join()...")
job.join()
log("After join — job is done")
""".trimIndent(),
                runner = { log ->
                    log("Before launch")
                    val job = launch {
                        delay(400)
                        log("Job completed after 400ms")
                    }
                    log("After launch — job still running")
                    log("Calling join()...")
                    job.join()
                    log("After join — job is done")
                }
            ),

            CoroutineProblem(
                id = "launch_nested",
                title = "Nested launch",
                category = "launch",
                description = "A launch inside another launch creates a child coroutine. " +
                        "The outer coroutine doesn't wait for nested launches unless you join them.",
                hint = "'Outer end' prints before 'Inner 2 (nested)' because nested launch is async",
                codeSnippet = """
log("Outer start")
val outer = launch {
    log("Inner 1 start")
    launch {
        delay(100)
        log("Inner 2 (nested — delayed)")
    }
    log("Inner 1 end (doesn't wait for nested)")
}
log("Outer end (immediately after outer launch)")
outer.join()
log("After outer.join() — but nested may still run via scope")
delay(200)
""".trimIndent(),
                runner = { log ->
                    log("Outer start")
                    val outer = launch {
                        log("Inner 1 start")
                        launch {
                            delay(100)
                            log("Inner 2 (nested — delayed)")
                        }
                        log("Inner 1 end (doesn't wait for nested)")
                    }
                    log("Outer end (immediately after outer launch)")
                    outer.join()
                    log("After outer.join() — nested still alive in scope")
                    delay(200)
                }
            ),
        )
    ),

    // ─── async / await ────────────────────────────────────────────────────────
    ProblemCategory(
        name = "async",
        emoji = "⚡",
        problems = listOf(

            CoroutineProblem(
                id = "async_sequential_antipattern",
                title = "Sequential async (anti-pattern)",
                category = "async",
                description = "Calling .await() immediately after async() makes it sequential — " +
                        "no concurrency benefit. Takes ~1000ms instead of ~500ms.",
                hint = "Elapsed should be ~1000ms. The fix is to launch both asyncs BEFORE any await",
                codeSnippet = """
val start = System.currentTimeMillis()
// ❌ Anti-pattern: await() immediately — fully sequential
val result1 = async { delay(500); "Result 1" }.await()
val result2 = async { delay(500); "Result 2" }.await()
log("result1=${'$'}result1")
log("result2=${'$'}result2")
log("Elapsed: ${'$'}{System.currentTimeMillis()-start}ms (expected ~1000ms)")
""".trimIndent(),
                runner = { log ->
                    val start = System.currentTimeMillis()
                    val result1 = async { delay(500); "Result 1" }.await()
                    val result2 = async { delay(500); "Result 2" }.await()
                    log("result1=$result1")
                    log("result2=$result2")
                    log("Elapsed: ${System.currentTimeMillis() - start}ms (expected ~1000ms)")
                }
            ),

            CoroutineProblem(
                id = "async_concurrent",
                title = "Concurrent async (correct pattern)",
                category = "async",
                description = "Start both async blocks BEFORE awaiting. " +
                        "They run concurrently — total time ≈ max(t1, t2) instead of t1+t2.",
                hint = "Elapsed should be ~500ms even though total work is ~1000ms",
                codeSnippet = """
val start = System.currentTimeMillis()
// ✅ Correct: start both before awaiting
val deferred1 = async { delay(500); "Result 1" }
val deferred2 = async { delay(500); "Result 2" }
val r1 = deferred1.await()
val r2 = deferred2.await()
log("r1=${'$'}r1, r2=${'$'}r2")
log("Elapsed: ${'$'}{System.currentTimeMillis()-start}ms (expected ~500ms)")
""".trimIndent(),
                runner = { log ->
                    val start = System.currentTimeMillis()
                    val deferred1 = async { delay(500); "Result 1" }
                    val deferred2 = async { delay(500); "Result 2" }
                    val r1 = deferred1.await()
                    val r2 = deferred2.await()
                    log("r1=$r1, r2=$r2")
                    log("Elapsed: ${System.currentTimeMillis() - start}ms (expected ~500ms)")
                }
            ),

            CoroutineProblem(
                id = "async_awaitall",
                title = "awaitAll() — await many deferreds",
                category = "async",
                description = "awaitAll() waits for all deferreds concurrently and returns a list. " +
                        "Order of results matches declaration order, not completion order.",
                hint = "All three run concurrently; fastest (C=100ms) finishes first but result list order is A,B,C",
                codeSnippet = """
val start = System.currentTimeMillis()
val deferreds = listOf(
    async { delay(300); log("A done"); "A" },
    async { delay(200); log("B done"); "B" },
    async { delay(100); log("C done"); "C" },
)
val results = deferreds.awaitAll()
log("Results (in order): ${'$'}results")
log("Elapsed: ${'$'}{System.currentTimeMillis()-start}ms (expected ~300ms)")
""".trimIndent(),
                runner = { log ->
                    val start = System.currentTimeMillis()
                    val deferreds = listOf(
                        async { delay(300); log("A done"); "A" },
                        async { delay(200); log("B done"); "B" },
                        async { delay(100); log("C done"); "C" },
                    )
                    val results = deferreds.awaitAll()
                    log("Results (in order): $results")
                    log("Elapsed: ${System.currentTimeMillis() - start}ms (expected ~300ms)")
                }
            ),

            CoroutineProblem(
                id = "async_exception_await",
                title = "async exception — only throws on await()",
                category = "async",
                description = "Exceptions in async{} are deferred — they don't throw until you call .await(). " +
                        "This means your code can continue past the async{} block before the error surfaces.",
                hint = "No exception before await; RuntimeException caught on await()",
                codeSnippet = """
coroutineScope {
    val deferred = async {
        delay(100)
        throw RuntimeException("Async error!")
        "never returned"
    }
    log("Deferred created — no exception yet")
    delay(50)
    log("50ms later — still no exception")
    delay(100)
    log("150ms — about to await...")
    try {
        deferred.await()
    } catch (e: Exception) {
        log("Caught on await(): ${'$'}{e.message}")
    }
}
""".trimIndent(),
                runner = { log ->
                    coroutineScope {
                        val deferred = async {
                            delay(100)
                            throw RuntimeException("Async error!")
                            @Suppress("UNREACHABLE_CODE")
                            "never returned"
                        }
                        log("Deferred created — no exception yet")
                        delay(50)
                        log("50ms later — still no exception")
                        delay(100)
                        log("150ms — about to await...")
                        try {
                            deferred.await()
                        } catch (e: Exception) {
                            log("Caught on await(): ${e.message}")
                        }
                    }
                }
            ),
        )
    ),

    // ─── withContext ──────────────────────────────────────────────────────────
    ProblemCategory(
        name = "withContext",
        emoji = "🔀",
        problems = listOf(

            CoroutineProblem(
                id = "withcontext_blocking",
                title = "withContext blocks the caller",
                category = "withContext",
                description = "withContext suspends the caller, switches dispatcher, runs the block, " +
                        "then resumes the caller with the result. Unlike launch, it IS sequential.",
                hint = "Steps print in strict order 1→2→3→4→5",
                codeSnippet = """
log("1: Before withContext")
val result = withContext(Dispatchers.Default) {
    log("2: Inside withContext (Default dispatcher)")
    delay(200)
    log("3: Still inside withContext")
    "computed result"
}
log("4: After withContext, result=${'$'}result")
log("5: Continues normally")
""".trimIndent(),
                runner = { log ->
                    log("1: Before withContext")
                    val result = withContext(Dispatchers.Default) {
                        log("2: Inside withContext (Default dispatcher)")
                        delay(200)
                        log("3: Still inside withContext")
                        "computed result"
                    }
                    log("4: After withContext, result=$result")
                    log("5: Continues normally")
                }
            ),

            CoroutineProblem(
                id = "withcontext_vs_launch",
                title = "withContext vs launch — key difference",
                category = "withContext",
                description = "withContext is sequential and returns a value. " +
                        "launch is concurrent and fire-and-forget. Choose wisely.",
                hint = "'After withContext' always follows the withContext body; 'After launch' can precede the launch body",
                codeSnippet = """
log("=== withContext (sequential) ===")
withContext(Dispatchers.Default) {
    delay(200)
    log("withContext body — 200ms later")
}
log("After withContext — GUARANTEED after body")

log("")
log("=== launch (concurrent) ===")
launch(Dispatchers.Default) {
    delay(200)
    log("launch body — 200ms later")
}
log("After launch — may print BEFORE launch body")
delay(300)
""".trimIndent(),
                runner = { log ->
                    log("=== withContext (sequential) ===")
                    withContext(Dispatchers.Default) {
                        delay(200)
                        log("withContext body — 200ms later")
                    }
                    log("After withContext — GUARANTEED after body")
                    log("")
                    log("=== launch (concurrent) ===")
                    launch(Dispatchers.Default) {
                        delay(200)
                        log("launch body — 200ms later")
                    }
                    log("After launch — may print BEFORE launch body")
                    delay(300)
                }
            ),

            CoroutineProblem(
                id = "withcontext_dispatcher_switching",
                title = "Dispatcher switching with withContext",
                category = "withContext",
                description = "Use withContext to switch dispatchers for specific work: " +
                        "IO for network/disk, Default for CPU, Main for UI updates.",
                hint = "Each block shows its thread name — they're different dispatchers",
                codeSnippet = """
log("Start — thread: ${'$'}{Thread.currentThread().name}")
withContext(Dispatchers.IO) {
    log("IO work — thread: ${'$'}{Thread.currentThread().name}")
    delay(100)
}
withContext(Dispatchers.Default) {
    log("CPU work — thread: ${'$'}{Thread.currentThread().name}")
    delay(100)
}
log("Back to original — thread: ${'$'}{Thread.currentThread().name}")
""".trimIndent(),
                runner = { log ->
                    log("Start — thread: ${Thread.currentThread().name}")
                    withContext(Dispatchers.IO) {
                        log("IO work — thread: ${Thread.currentThread().name}")
                        delay(100)
                    }
                    withContext(Dispatchers.Default) {
                        log("CPU work — thread: ${Thread.currentThread().name}")
                        delay(100)
                    }
                    log("Back to original — thread: ${Thread.currentThread().name}")
                }
            ),
        )
    ),

    // ─── Scope ────────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "Scope",
        emoji = "🔭",
        problems = listOf(

            CoroutineProblem(
                id = "scope_coroutine_scope",
                title = "coroutineScope — wait for all children",
                category = "Scope",
                description = "coroutineScope{} suspends until ALL launched children complete. " +
                        "Code after it is guaranteed to run after all children finish.",
                hint = "'After coroutineScope' only prints after both children finish",
                codeSnippet = """
log("Before coroutineScope")
coroutineScope {
    launch { delay(300); log("Child 1 done (300ms)") }
    launch { delay(100); log("Child 2 done (100ms)") }
    log("Inside scope — launches queued, not started yet")
}
log("After coroutineScope — ALL children done")
""".trimIndent(),
                runner = { log ->
                    log("Before coroutineScope")
                    coroutineScope {
                        launch { delay(300); log("Child 1 done (300ms)") }
                        launch { delay(100); log("Child 2 done (100ms)") }
                        log("Inside scope — launches queued, not started yet")
                    }
                    log("After coroutineScope — ALL children done")
                }
            ),

            CoroutineProblem(
                id = "scope_child_failure_cancels",
                title = "coroutineScope — child failure cancels siblings",
                category = "Scope",
                description = "In coroutineScope, if ANY child fails, the scope cancels all " +
                        "other children and rethrows the exception.",
                hint = "Child 1 (500ms) never prints because Child 2 (100ms) throws first",
                codeSnippet = """
try {
    coroutineScope {
        launch {
            delay(500)
            log("Child 1: NEVER reached (cancelled by sibling)")
        }
        launch {
            delay(100)
            log("Child 2: throwing now!")
            throw RuntimeException("Child 2 failed")
        }
        log("Scope started — both children running")
    }
} catch (e: Exception) {
    log("coroutineScope threw: ${'$'}{e.message}")
}
log("Execution resumes after try/catch")
""".trimIndent(),
                runner = { log ->
                    try {
                        coroutineScope {
                            launch {
                                delay(500)
                                log("Child 1: NEVER reached (cancelled by sibling)")
                            }
                            launch {
                                delay(100)
                                log("Child 2: throwing now!")
                                throw RuntimeException("Child 2 failed")
                            }
                            log("Scope started — both children running")
                        }
                    } catch (e: Exception) {
                        log("coroutineScope threw: ${e.message}")
                    }
                    log("Execution resumes after try/catch")
                }
            ),

            CoroutineProblem(
                id = "scope_supervisor_isolation",
                title = "supervisorScope — child failures are isolated",
                category = "Scope",
                description = "supervisorScope lets children fail independently. " +
                        "A failing child doesn't cancel its siblings.",
                hint = "Child 1 (500ms) DOES print even though Child 2 failed at 100ms",
                codeSnippet = """
supervisorScope {
    launch {
        delay(500)
        log("Child 1: still runs! (supervisor isolates failures)")
    }
    launch {
        delay(100)
        log("Child 2: throwing now!")
        throw RuntimeException("Child 2 failed — but sibling keeps running")
    }
    log("Scope started")
}
log("After supervisorScope")
""".trimIndent(),
                runner = { log ->
                    supervisorScope {
                        launch {
                            delay(500)
                            log("Child 1: still runs! (supervisor isolates failures)")
                        }
                        launch {
                            delay(100)
                            log("Child 2: throwing now!")
                            throw RuntimeException("Child 2 failed — but sibling keeps running")
                        }
                        log("Scope started")
                    }
                    log("After supervisorScope")
                }
            ),
        )
    ),

    // ─── Cancellation ─────────────────────────────────────────────────────────
    ProblemCategory(
        name = "Cancel",
        emoji = "🛑",
        problems = listOf(

            CoroutineProblem(
                id = "cancel_job",
                title = "cancel() a running Job",
                category = "Cancel",
                description = "Calling job.cancel() requests cancellation. The coroutine is " +
                        "cancelled at the next suspension point (delay, yield, etc.).",
                hint = "Only iterations 0,1,2 print — cancelled at ~350ms before iteration 3",
                codeSnippet = """
val job = launch {
    repeat(10) { i ->
        delay(100)
        log("Iteration ${'$'}i")
    }
}
delay(350)
log("Cancelling job...")
job.cancel()
job.join()
log("Job cancelled. isCancelled=${true}")
""".trimIndent(),
                runner = { log ->
                    val job = launch {
                        repeat(10) { i ->
                            delay(100)
                            log("Iteration $i")
                        }
                    }
                    delay(350)
                    log("Cancelling job...")
                    job.cancel()
                    job.join()
                    log("Job done. isCancelled=${job.isCancelled}")
                }
            ),

            CoroutineProblem(
                id = "cancel_isactive",
                title = "isActive — cancel tight loops",
                category = "Cancel",
                description = "Cancellation only works at suspension points. For CPU-heavy loops " +
                        "with no suspend calls, check isActive manually.",
                hint = "Without isActive check a tight loop won't honour cancellation",
                codeSnippet = """
val job = launch(Dispatchers.Default) {
    var count = 0
    // isActive = false when cancelled
    while (isActive) {
        count++
        if (count % 500_000 == 0) log("count=${'$'}count")
        if (count >= 2_000_000) break
    }
    log("Loop ended, isActive=${'$'}isActive, count=${'$'}count")
}
delay(50)
log("Cancelling tight loop...")
job.cancelAndJoin()
log("Done")
""".trimIndent(),
                runner = { log ->
                    val job = launch(Dispatchers.Default) {
                        var count = 0
                        while (isActive) {
                            count++
                            if (count % 500_000 == 0) log("count=$count")
                            if (count >= 2_000_000) break
                        }
                        log("Loop ended, isActive=$isActive, count=$count")
                    }
                    delay(50)
                    log("Cancelling tight loop...")
                    job.cancelAndJoin()
                    log("Done")
                }
            ),

            CoroutineProblem(
                id = "cancel_finally_noncancellable",
                title = "CancellationException + finally + NonCancellable",
                category = "Cancel",
                description = "finally{} always runs on cancellation. But inside finally you can't " +
                        "call suspend functions — use withContext(NonCancellable) for cleanup.",
                hint = "finally block always runs; only NonCancellable lets you suspend inside it",
                codeSnippet = """
val job = launch {
    try {
        log("Working...")
        delay(1000)
        log("NEVER: cancelled before this")
    } catch (e: CancellationException) {
        log("CancellationException caught — rethrowing (required!)")
        throw e
    } finally {
        log("finally: always runs on cancel")
        withContext(NonCancellable) {
            log("NonCancellable: can suspend here safely")
            delay(100)
            log("NonCancellable: cleanup done")
        }
    }
}
delay(100)
log("Cancelling job...")
job.cancelAndJoin()
log("Finished. isCancelled=${true}")
""".trimIndent(),
                runner = { log ->
                    val job = launch {
                        try {
                            log("Working...")
                            delay(1000)
                            log("NEVER: cancelled before this")
                        } catch (e: CancellationException) {
                            log("CancellationException caught — rethrowing (required!)")
                            throw e
                        } finally {
                            log("finally: always runs on cancel")
                            withContext(NonCancellable) {
                                log("NonCancellable: can suspend here safely")
                                delay(100)
                                log("NonCancellable: cleanup done")
                            }
                        }
                    }
                    delay(100)
                    log("Cancelling job...")
                    job.cancelAndJoin()
                    log("Finished. isCancelled=${job.isCancelled}")
                }
            ),
        )
    ),

    // ─── Exception Handling ───────────────────────────────────────────────────
    ProblemCategory(
        name = "Exceptions",
        emoji = "💥",
        problems = listOf(

            CoroutineProblem(
                id = "exception_handler",
                title = "CoroutineExceptionHandler",
                category = "Exceptions",
                description = "CoroutineExceptionHandler catches unhandled exceptions from root coroutines " +
                        "(or children of a SupervisorJob). It must be on the root launch.",
                hint = "Handler fires; execution continues after job.join()",
                codeSnippet = """
val handler = CoroutineExceptionHandler { _, e ->
    log("Handler caught: ${'$'}{e.message}")
}
supervisorScope {
    val job = launch(handler) {
        log("Coroutine: about to throw")
        delay(100)
        throw RuntimeException("Boom!")
    }
    job.join()
    log("After job.join() — handler absorbed the exception")
}
log("All done — execution continues normally")
""".trimIndent(),
                runner = { log ->
                    val handler = CoroutineExceptionHandler { _, e ->
                        log("Handler caught: ${e.message}")
                    }
                    supervisorScope {
                        val job = launch(handler) {
                            log("Coroutine: about to throw")
                            delay(100)
                            throw RuntimeException("Boom!")
                        }
                        job.join()
                        log("After job.join() — handler absorbed the exception")
                    }
                    log("All done — execution continues normally")
                }
            ),

            CoroutineProblem(
                id = "exception_trycatch_launch",
                title = "try-catch around launch — DOES NOT WORK",
                category = "Exceptions",
                description = "Wrapping launch{} in try-catch does NOT catch exceptions thrown inside the " +
                        "coroutine. launch{} returns immediately; the exception propagates via the Job.",
                hint = "The catch block never runs — exception propagates through the scope",
                codeSnippet = """
supervisorScope {
    try {
        launch {
            delay(100)
            throw RuntimeException("Inside launch")
        }
        // ↑ launch() returns Job immediately; no exception here
    } catch (e: Exception) {
        log("❌ This catch NEVER runs!")
    }

    // Need a handler or supervisorScope to absorb it:
    val handler = CoroutineExceptionHandler { _, e ->
        log("✅ Handler caught: ${'$'}{e.message}")
    }
    launch(handler) {
        delay(200)
        throw RuntimeException("Inside launch with handler")
    }
    delay(400)
}
""".trimIndent(),
                runner = { log ->
                    supervisorScope {
                        try {
                            launch {
                                delay(100)
                                throw RuntimeException("Inside launch")
                            }
                        } catch (e: Exception) {
                            log("❌ This catch NEVER runs!")
                        }
                        val handler = CoroutineExceptionHandler { _, e ->
                            log("✅ Handler caught: ${e.message}")
                        }
                        launch(handler) {
                            delay(200)
                            throw RuntimeException("Inside launch with handler")
                        }
                        delay(400)
                        log("Execution here shows supervisor absorbed both failures")
                    }
                }
            ),

            CoroutineProblem(
                id = "exception_supervisor_isolation",
                title = "Exception propagation: regular vs supervisor",
                category = "Exceptions",
                description = "Compare how exceptions propagate differently in coroutineScope vs supervisorScope.",
                hint = "In coroutineScope: failure cancels siblings. In supervisorScope: each child is independent",
                codeSnippet = """
log("=== coroutineScope: failure cancels siblings ===")
try {
    coroutineScope {
        launch { delay(300); log("Sibling: NEVER runs") }
        launch { delay(100); throw RuntimeException("Error!") }
    }
} catch (e: Exception) { log("Caught: ${'$'}{e.message}") }

log("")
log("=== supervisorScope: siblings survive ===")
supervisorScope {
    val h = CoroutineExceptionHandler { _, e -> log("Handler: ${'$'}{e.message}") }
    launch { delay(300); log("Sibling: still runs!") }
    launch(h) { delay(100); throw RuntimeException("Error!") }
    delay(400)
}
""".trimIndent(),
                runner = { log ->
                    log("=== coroutineScope: failure cancels siblings ===")
                    try {
                        coroutineScope {
                            launch { delay(300); log("Sibling: NEVER runs") }
                            launch { delay(100); throw RuntimeException("Error!") }
                        }
                    } catch (e: Exception) { log("Caught: ${e.message}") }
                    log("")
                    log("=== supervisorScope: siblings survive ===")
                    supervisorScope {
                        val h = CoroutineExceptionHandler { _, e -> log("Handler: ${e.message}") }
                        launch { delay(300); log("Sibling: still runs!") }
                        launch(h) { delay(100); throw RuntimeException("Error!") }
                        delay(400)
                    }
                }
            ),
        )
    ),

    // ─── Flow ─────────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "Flow",
        emoji = "🌊",
        problems = listOf(

            CoroutineProblem(
                id = "flow_basic",
                title = "Basic Flow — cold stream",
                category = "Flow",
                description = "Flow is cold — nothing runs until collect{} is called. " +
                        "Each collector gets its own independent execution.",
                hint = "'Flow started' and 'Flow completed' wrap all emissions",
                codeSnippet = """
val numbersFlow = flow {
    log("Flow started")
    emit(1); delay(100)
    emit(2); delay(100)
    emit(3)
    log("Flow completed")
}

log("Before first collect")
numbersFlow.collect { log("Collected: ${'$'}it") }
log("After first collect")
log("")
log("Second collect — flow restarts from scratch")
numbersFlow.collect { log("Collected again: ${'$'}it") }
""".trimIndent(),
                runner = { log ->
                    val numbersFlow = flow {
                        log("Flow started")
                        emit(1); delay(100)
                        emit(2); delay(100)
                        emit(3)
                        log("Flow completed")
                    }
                    log("Before first collect")
                    numbersFlow.collect { log("Collected: $it") }
                    log("After first collect")
                    log("")
                    log("Second collect — flow restarts from scratch")
                    numbersFlow.collect { log("Collected again: $it") }
                }
            ),

            CoroutineProblem(
                id = "flow_operators",
                title = "Flow operators: filter, map, take",
                category = "Flow",
                description = "Operators transform the flow lazily. take() stops collection early — " +
                        "even the producer stops emitting once the quota is reached.",
                hint = "Only even squares up to take(3) limit: 0, 4, 16",
                codeSnippet = """
flow {
    repeat(10) {
        log("Emitting ${'$'}it")
        emit(it)
    }
}
.filter { it % 2 == 0 }
.map { value -> value * value }
.take(3)
.collect { log("→ ${'$'}it") }
log("Done — notice emitter stopped after take(3) was satisfied")
""".trimIndent(),
                runner = { log ->
                    flow {
                        repeat(10) {
                            log("Emitting $it")
                            emit(it)
                        }
                    }
                        .filter { it % 2 == 0 }
                        .map { value -> value * value }
                        .take(3)
                        .collect { log("→ $it") }
                    log("Done — notice emitter stopped after take(3) was satisfied")
                }
            ),

            CoroutineProblem(
                id = "flow_stateflow",
                title = "StateFlow — hot state holder",
                category = "Flow",
                description = "StateFlow is hot — it's always active, holds the latest value, and " +
                        "only emits distinct values. New collectors get the current value immediately.",
                hint = "Duplicate value 2 emitted twice but collected only once",
                codeSnippet = """
val state = MutableStateFlow(0)

val job = launch {
    state.collect { log("Observer: state=${'$'}it") }
}

delay(50); state.value = 1
delay(50); state.value = 2
delay(50); state.value = 2  // duplicate — won't emit
delay(50); state.value = 3
delay(100)
log("Final state: ${'$'}{state.value}")
job.cancel()
""".trimIndent(),
                runner = { log ->
                    val state = MutableStateFlow(0)
                    val job = launch {
                        state.collect { log("Observer: state=$it") }
                    }
                    delay(50); state.value = 1
                    delay(50); state.value = 2
                    delay(50); state.value = 2
                    delay(50); state.value = 3
                    delay(100)
                    log("Final state: ${state.value}")
                    job.cancel()
                }
            ),
        )
    ),

    // ─── Channels ─────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "Channels",
        emoji = "📡",
        problems = listOf(

            CoroutineProblem(
                id = "channel_basic",
                title = "Channel — send & receive",
                category = "Channels",
                description = "Channel is a rendezvous by default: send() suspends until a receiver is ready. " +
                        "The for loop collects until the channel is closed.",
                hint = "Producer sends one at a time; consumer receives in order",
                codeSnippet = """
val channel = Channel<Int>()

launch {
    log("Producer: sending 1"); channel.send(1)
    log("Producer: sending 2"); channel.send(2)
    log("Producer: sending 3"); channel.send(3)
    channel.close()
    log("Producer: channel closed")
}

for (value in channel) {
    log("Consumer: received ${'$'}value")
    delay(100)
}
log("Consumer: channel closed, done")
""".trimIndent(),
                runner = { log ->
                    val channel = Channel<Int>()
                    launch {
                        log("Producer: sending 1"); channel.send(1)
                        log("Producer: sending 2"); channel.send(2)
                        log("Producer: sending 3"); channel.send(3)
                        channel.close()
                        log("Producer: channel closed")
                    }
                    for (value in channel) {
                        log("Consumer: received $value")
                        delay(100)
                    }
                    log("Consumer: channel closed, done")
                }
            ),

            CoroutineProblem(
                id = "channel_buffered",
                title = "Buffered Channel — capacity matters",
                category = "Channels",
                description = "A buffered channel lets the sender proceed without waiting for a receiver, " +
                        "up to its capacity. Once full, send() suspends again.",
                hint = "With capacity=3 the producer sends 3 values without blocking before the consumer starts",
                codeSnippet = """
val channel = Channel<Int>(capacity = 3)

launch {
    repeat(6) { i ->
        log("Sending ${'$'}i (may suspend when buffer full)")
        channel.send(i)
        log("Sent ${'$'}i")
    }
    channel.close()
}

delay(50)
log("=== Consumer starting ===")
for (v in channel) {
    log("Received: ${'$'}v")
    delay(80)
}
""".trimIndent(),
                runner = { log ->
                    val channel = Channel<Int>(capacity = 3)
                    launch {
                        repeat(6) { i ->
                            log("Sending $i (may suspend when buffer full)")
                            channel.send(i)
                            log("Sent $i")
                        }
                        channel.close()
                    }
                    delay(50)
                    log("=== Consumer starting ===")
                    for (v in channel) {
                        log("Received: $v")
                        delay(80)
                    }
                }
            ),
        )
    ),

    // ─── select ───────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "select",
        emoji = "🎯",
        problems = listOf(

            CoroutineProblem(
                id = "select_first_wins",
                title = "select{} — first ready wins",
                category = "select",
                description = "select{} suspends until one of multiple clauses is ready, " +
                        "then executes that clause and returns. Like a coroutine switch-case.",
                hint = "ch2 resolves first (100ms < 200ms), so select picks ch2",
                codeSnippet = """
val ch1 = Channel<String>()
val ch2 = Channel<String>()

launch { delay(200); ch1.send("from channel 1 (200ms)") }
launch { delay(100); ch2.send("from channel 2 (100ms)") }

repeat(2) {
    val result = select {
        ch1.onReceive { "ch1: ${'$'}it" }
        ch2.onReceive { "ch2: ${'$'}it" }
    }
    log("Selected: ${'$'}result")
}
""".trimIndent(),
                runner = { log ->
                    val ch1 = Channel<String>()
                    val ch2 = Channel<String>()
                    launch { delay(200); ch1.send("from channel 1 (200ms)") }
                    launch { delay(100); ch2.send("from channel 2 (100ms)") }
                    repeat(2) {
                        val result = select {
                            ch1.onReceive { "ch1: $it" }
                            ch2.onReceive { "ch2: $it" }
                        }
                        log("Selected: $result")
                    }
                }
            ),

            CoroutineProblem(
                id = "select_deferred",
                title = "select{} on Deferred — race two async tasks",
                category = "select",
                description = "Use select on Deferred.onAwait to race multiple async computations. " +
                        "The first to complete wins; the other is cancelled.",
                hint = "fast (50ms) always beats slow (300ms) — select picks the first ready",
                codeSnippet = """
coroutineScope {
    val fast = async { delay(50);  log("fast: finished"); "fast result" }
    val slow = async { delay(300); log("slow: finished"); "slow result" }

    val winner = select<String> {
        fast.onAwait { "⚡ fast won: ${'$'}it" }
        slow.onAwait { "🐢 slow won: ${'$'}it" }
    }
    log("Winner → ${'$'}winner")
    slow.cancel()
    log("slow cancelled: ${'$'}{slow.isCancelled}")
}
""".trimIndent(),
                runner = { log ->
                    coroutineScope {
                        val fast = async { delay(50); log("fast: finished"); "fast result" }
                        val slow = async { delay(300); log("slow: finished"); "slow result" }
                        val winner = select<String> {
                            fast.onAwait { "⚡ fast won: $it" }
                            slow.onAwait { "🐢 slow won: $it" }
                        }
                        log("Winner → $winner")
                        slow.cancel()
                        log("slow cancelled: ${slow.isCancelled}")
                    }
                }
            ),
        )
    ),

    // ─── Mutex ────────────────────────────────────────────────────────────────
    ProblemCategory(
        name = "Mutex",
        emoji = "🔒",
        problems = listOf(

            CoroutineProblem(
                id = "mutex_race_condition",
                title = "Race condition without Mutex",
                category = "Mutex",
                description = "Multiple coroutines reading and writing a shared variable concurrently " +
                        "produce incorrect results. The ++ operator is NOT atomic.",
                hint = "Counter will almost certainly NOT be 100000 — data race in action",
                codeSnippet = """
var counter = 0
val jobs = List(100) {
    launch(Dispatchers.Default) {
        repeat(1000) { counter++ }   // ❌ not atomic!
    }
}
jobs.forEach { it.join() }
log("Expected: 100_000")
log("Actual  : ${'$'}counter")
log("Race condition: ${'$'}{counter != 100_000}")
""".trimIndent(),
                runner = { log ->
                    var counter = 0
                    val jobs = List(100) {
                        launch(Dispatchers.Default) {
                            repeat(1000) { counter++ }
                        }
                    }
                    jobs.forEach { it.join() }
                    log("Expected: 100_000")
                    log("Actual  : $counter")
                    log("Race condition: ${counter != 100_000}")
                }
            ),

            CoroutineProblem(
                id = "mutex_fix",
                title = "Mutex — fixing the race condition",
                category = "Mutex",
                description = "Mutex.withLock{} ensures only one coroutine enters the critical section at a time. " +
                        "It suspends (not blocks) while waiting.",
                hint = "Counter is exactly 100000 every time — Mutex guarantees correctness",
                codeSnippet = """
val mutex = Mutex()
var counter = 0
val jobs = List(100) {
    launch(Dispatchers.Default) {
        repeat(1000) {
            mutex.withLock { counter++ }  // ✅ atomic via Mutex
        }
    }
}
jobs.forEach { it.join() }
log("Expected: 100_000")
log("Actual  : ${'$'}counter")
log("Correct : ${'$'}{counter == 100_000}")
""".trimIndent(),
                runner = { log ->
                    val mutex = Mutex()
                    var counter = 0
                    val jobs = List(100) {
                        launch(Dispatchers.Default) {
                            repeat(1000) {
                                mutex.withLock { counter++ }
                            }
                        }
                    }
                    jobs.forEach { it.join() }
                    log("Expected: 100_000")
                    log("Actual  : $counter")
                    log("Correct : ${counter == 100_000}")
                }
            ),
        )
    ),
)
