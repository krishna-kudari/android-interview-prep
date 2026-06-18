package com.example.interview.coroutineslab

import kotlinx.coroutines.CoroutineScope

data class OutputLine(
    val text: String,
    val isError: Boolean = false,
)

data class CoroutineProblem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val hint: String = "",
    val codeSnippet: String,
    val runner: suspend CoroutineScope.(log: (String) -> Unit) -> Unit,
)

data class ProblemCategory(
    val name: String,
    val emoji: String,
    val problems: List<CoroutineProblem>,
)
