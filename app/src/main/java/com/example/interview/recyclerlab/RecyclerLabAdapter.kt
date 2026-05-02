package com.example.interview.recyclerlab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.interview.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Multi-type adapter demonstrating:
 * - Classic ViewHolder subclasses with one-time [findViewById] work in constructors
 * - [ListAdapter] + [androidx.recyclerview.widget.DiffUtil] via [LabItemDiffCallback]
 * - Partial updates through [onBindViewHolder] + payloads (see [SimpleRowViewHolder])
 * - Nested horizontal [RecyclerView] sharing the outer list’s [RecyclerView.RecycledViewPool]
 */
class RecyclerLabAdapter(
    private val parentRecyclerView: RecyclerView,
    private val stressBind: () -> Boolean,
) : ListAdapter<LabListItem, RecyclerView.ViewHolder>(LabItemDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).stableId

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is LabListItem.EducationCard -> RecyclerLabViewTypes.EDUCATION
        is LabListItem.SectionHeader -> RecyclerLabViewTypes.SECTION
        is LabListItem.SimpleRow -> RecyclerLabViewTypes.SIMPLE
        is LabListItem.NestedCarouselRow -> RecyclerLabViewTypes.NESTED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RecyclerLabViewTypes.EDUCATION -> EducationViewHolder(
                inflater.inflate(R.layout.item_recycler_lab_education, parent, false),
            )
            RecyclerLabViewTypes.SECTION -> SectionViewHolder(
                inflater.inflate(R.layout.item_recycler_lab_section, parent, false),
            )
            RecyclerLabViewTypes.SIMPLE -> SimpleRowViewHolder(
                inflater.inflate(R.layout.item_recycler_lab_simple_row, parent, false),
            )
            RecyclerLabViewTypes.NESTED -> NestedCarouselViewHolder(
                inflater.inflate(R.layout.item_recycler_lab_nested, parent, false),
                parentRecyclerView,
            )
            else -> error("Unknown viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EducationViewHolder -> holder.bind(getItem(position) as LabListItem.EducationCard)
            is SectionViewHolder -> holder.bind(getItem(position) as LabListItem.SectionHeader)
            is SimpleRowViewHolder -> {
                maybeStressBind()
                holder.bind(getItem(position) as LabListItem.SimpleRow)
            }
            is NestedCarouselViewHolder -> {
                maybeStressBind()
                holder.bind(getItem(position) as LabListItem.NestedCarouselRow)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        if (holder is SimpleRowViewHolder) {
            var applied = false
            for (payload in payloads) {
                if (payload is Bundle && payload.containsKey(LabItemDiffCallback.PAYLOAD_VALUE)) {
                    holder.bindValue(payload.getInt(LabItemDiffCallback.PAYLOAD_VALUE))
                    applied = true
                }
            }
            if (!applied) {
                onBindViewHolder(holder, position)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    private fun maybeStressBind() {
        if (!stressBind()) return
        try {
            Thread.sleep(4L)
        } catch (_: InterruptedException) {
            // ignore
        }
    }

    private class EducationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_education_title)
        private val body: TextView = itemView.findViewById(R.id.text_education_body)

        fun bind(item: LabListItem.EducationCard) {
            title.text = item.title
            body.text = item.body
        }
    }

    private class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_section_title)

        fun bind(item: LabListItem.SectionHeader) {
            title.text = item.title
        }
    }

    private class SimpleRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_simple_row)
        private val title: TextView = itemView.findViewById(R.id.text_simple_title)
        private val value: TextView = itemView.findViewById(R.id.text_simple_value)
        private val bar: LinearProgressIndicator = itemView.findViewById(R.id.progress_simple)

        fun bind(item: LabListItem.SimpleRow) {
            title.text = item.title
            bindValue(item.value)
            card.strokeColor = item.accentColor
        }

        fun bindValue(newValue: Int) {
            value.text = newValue.toString()
            val clamped = (newValue % 101).coerceIn(0, 100)
            bar.setProgressCompat(clamped, true)
        }
    }

    private class NestedCarouselViewHolder(
        itemView: View,
        outer: RecyclerView,
    ) : RecyclerView.ViewHolder(itemView) {
        private val inner: RecyclerView = itemView.findViewById(R.id.recycler_nested_horizontal)
        private val adapter = HorizontalChipAdapter()

        init {
            inner.adapter = adapter
            inner.layoutManager = LinearLayoutManager(
                itemView.context,
                RecyclerView.HORIZONTAL,
                false,
            )
            inner.setHasFixedSize(true)
            inner.itemAnimator = null
            // Share scrap pool with the vertical list so off-screen inner rows return views
            // to the same pool family (see RecyclerViewLabActivity pool commentary).
            inner.setRecycledViewPool(outer.recycledViewPool)
        }

        fun bind(item: LabListItem.NestedCarouselRow) {
            adapter.submitList(item.chips)
        }
    }

}
