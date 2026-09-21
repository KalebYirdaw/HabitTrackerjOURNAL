package com.example.habittracker.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemGalleryBinding
import com.example.habittracker.models.JournalEntry

class GalleryAdapter(
    private val onItemClick: (JournalEntry) -> Unit
) : ListAdapter<JournalEntry, GalleryAdapter.GalleryViewHolder>(GalleryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GalleryViewHolder(
        private val binding: ItemGalleryBinding,
        private val onItemClick: (JournalEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: JournalEntry) {
            val uriString = entry.imageUri
            if (uriString != null) {
                try {
                    binding.ivGalleryImage.setImageURI(Uri.parse(uriString))
                } catch (e: Exception) {
                    binding.ivGalleryImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                binding.ivGalleryImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.root.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean = oldItem == newItem
    }
}