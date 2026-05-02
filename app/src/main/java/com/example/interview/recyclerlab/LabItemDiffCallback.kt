package com.example.interview.recyclerlab

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil

/**
 * [DiffUtil.ItemCallback] for [LabListItem]. Demonstrates:
 * - identity vs content equality
 * - [getChangePayload] for partial binds (see [PAYLOAD_VALUE])
 */
class LabItemDiffCallback : DiffUtil.ItemCallback<LabListItem>() {

    override fun areItemsTheSame(oldItem: LabListItem, newItem: LabListItem): Boolean =
        oldItem.stableId == newItem.stableId

    override fun areContentsTheSame(oldItem: LabListItem, newItem: LabListItem): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: LabListItem, newItem: LabListItem): Any? {
        if (oldItem is LabListItem.SimpleRow && newItem is LabListItem.SimpleRow) {
            if (oldItem.title == newItem.title &&
                oldItem.accentColor == newItem.accentColor &&
                oldItem.value != newItem.value
            ) {
                return Bundle().apply { putInt(PAYLOAD_VALUE, newItem.value) }
            }
        }
        return null
    }

    companion object {
        const val PAYLOAD_VALUE = "payload_value"
    }
}
