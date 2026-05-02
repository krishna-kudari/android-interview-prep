package com.example.interview.pulsenews.feature.feed

sealed class SearchBarState {
    object Collapsed : SearchBarState()
    data class Expanded(val query: String) : SearchBarState()
}