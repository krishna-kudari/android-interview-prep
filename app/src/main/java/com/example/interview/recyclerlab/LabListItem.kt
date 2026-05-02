package com.example.interview.recyclerlab

import androidx.annotation.ColorInt

/**
 * Immutable list rows for [RecyclerLabAdapter]. Each item exposes a [stableId] for
 * [androidx.recyclerview.widget.DiffUtil] and stable-id scrolling optimizations.
 */
sealed class LabListItem {
    abstract val stableId: Long

    data class EducationCard(
        override val stableId: Long,
        val title: String,
        val body: String,
    ) : LabListItem()

    data class SectionHeader(
        override val stableId: Long,
        val title: String,
    ) : LabListItem()

    data class SimpleRow(
        override val stableId: Long,
        val title: String,
        val value: Int,
        @ColorInt val accentColor: Int,
    ) : LabListItem()

    data class NestedCarouselRow(
        override val stableId: Long,
        val chips: List<String>,
    ) : LabListItem()
}
