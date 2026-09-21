package com.example.habittracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemJournalBinding
import com.example.habittracker.databinding.ItemJournalHeaderBinding
import com.example.habittracker.models.JournalEntry
import java.text.SimpleDateFormat
import java.util.Locale

sealed class JournalListItem {
    data class Header(val title: String) : JournalListItem()
    data class Entry(val entry: JournalEntry) : JournalListItem()
}

class JournalAdapter(
    private val onItemClick: (JournalEntry) -> Unit
) : ListAdapter<JournalListItem, RecyclerView.ViewHolder>(JournalDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ENTRY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is JournalListItem.Header -> VIEW_TYPE_HEADER
            is JournalListItem.Entry -> VIEW_TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemJournalHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemJournalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            JournalViewHolder(binding, onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if ((holder is HeaderViewHolder) && (item is JournalListItem.Header)) {
            holder.bind(item.title)
        } else if ((holder is JournalViewHolder) && (item is JournalListItem.Entry)) {
            holder.bind(item.entry)
        }
    }

    class HeaderViewHolder(private val headerBinding: ItemJournalHeaderBinding) :
        RecyclerView.ViewHolder(headerBinding.root) {
        fun bind(title: String) {
            headerBinding.tvHeaderTitle.text = title
        }
    }

    class JournalViewHolder(
        private val binding: ItemJournalBinding,
        private val onItemClick: (JournalEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(entry: JournalEntry) {
            binding.tvEntryTitle.text = entry.title
            binding.tvEntryContent.text = entry.content
            binding.tvEntryDate.text = dateFormat.format(entry.timestamp)

            binding.root.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    class JournalDiffCallback : DiffUtil.ItemCallback<JournalListItem>() {
        override fun areItemsTheSame(oldItem: JournalListItem, newItem: JournalListItem): Boolean {
            return if (oldItem is JournalListItem.Header && newItem is JournalListItem.Header) {
                oldItem.title == newItem.title
            } else if (oldItem is JournalListItem.Entry && newItem is JournalListItem.Entry) {
                oldItem.entry.id == newItem.entry.id
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: JournalListItem, newItem: JournalListItem): Boolean = oldItem == newItem
    }
}