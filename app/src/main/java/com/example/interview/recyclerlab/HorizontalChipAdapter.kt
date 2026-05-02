package com.example.interview.recyclerlab

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.interview.R

/**
 * Small inner list for nested horizontal rows. Keeps work out of the vertical adapter’s
 * [RecyclerView.Adapter.onBindViewHolder] beyond wiring the inner [RecyclerView].
 */
class HorizontalChipAdapter : ListAdapter<String, HorizontalChipAdapter.ChipViewHolder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_lab_chip, parent, false)
        return ChipViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChipViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(label: String) {
            textView.text = label
        }
    }
}
