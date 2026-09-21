package com.example.habittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.activity.JournalDetailActivity
import com.example.habittracker.api.LocalJournalManager
import com.example.habittracker.databinding.FragmentJournalBinding
import com.example.habittracker.databinding.DialogAddJournalBinding
import com.example.habittracker.models.JournalEntry
import java.util.Calendar
import android.content.Intent
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.habittracker.R

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val journalDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            fetchJournals()
        }
    }

    private val journalAdapter = JournalAdapter(
        onItemClick = { entry ->
            val intent = Intent(requireContext(), JournalDetailActivity::class.java).apply {
                putExtra("entry", entry)
            }
            journalDetailLauncher.launch(intent)
        }
    )

    private val galleryAdapter = GalleryAdapter(
        onItemClick = { entry ->
            val intent = Intent(requireContext(), JournalDetailActivity::class.java).apply {
                putExtra("entry", entry)
            }
            journalDetailLauncher.launch(intent)
        }
    )

    private lateinit var localJournalManager: LocalJournalManager
    private var isGalleryVisible = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val entry = JournalEntry(
                title = "Photo Entry",
                content = "Memory added to Gallery.",
                imageUri = it.toString()
            )
            localJournalManager.saveEntry(entry)
            fetchJournals()
            Toast.makeText(requireContext(), "Image added to gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        localJournalManager = LocalJournalManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        updateTabVisibility()

        binding.btnAddComment.setOnClickListener {
            showAddEntryDialog(isPhotoEntry = false)
        }

        binding.btnAddPhoto.setOnClickListener {
            showAddEntryDialog(isPhotoEntry = true)
        }

        binding.tabDailyComments.setOnClickListener {
            isGalleryVisible = false
            updateTabVisibility()
        }

        binding.tabGallery.setOnClickListener {
            isGalleryVisible = true
            updateTabVisibility()
        }
    }

    private fun showAddEntryDialog(isPhotoEntry: Boolean) {
        if (isPhotoEntry) {
            pickImageLauncher.launch("image/*")
            return
        }

        val dialogBinding = DialogAddJournalBinding.inflate(layoutInflater)
        dialogBinding.btnDialogAddImage.visibility = View.GONE

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnDialogSubmit.setOnClickListener {
            val title = dialogBinding.etDialogJournalTitle.text.toString().trim()
            val content = dialogBinding.etDialogJournalContent.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val entry = JournalEntry(
                    title = title,
                    content = content,
                    imageUri = null
                )
                localJournalManager.saveEntry(entry)
                fetchJournals()
                Toast.makeText(requireContext(), "Entry posted locally!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateTabVisibility() {
        if (isGalleryVisible) {
            binding.layoutDailyComments.visibility = View.GONE
            binding.layoutGalleryContainer.visibility = View.VISIBLE
            setActiveTab(binding.tabGallery)
        } else {
            binding.layoutGalleryContainer.visibility = View.GONE
            binding.layoutDailyComments.visibility = View.VISIBLE
            setActiveTab(binding.tabDailyComments)
        }
        fetchJournals()
    }

    private fun setActiveTab(selected: TextView) {
        val tabs = listOf(binding.tabDailyComments, binding.tabGallery)
        for (tab in tabs) {
            tab.background = if (tab === selected)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            tab.setTextColor(
                ContextCompat.getColor(requireContext(), if (tab === selected) R.color.white else R.color.black)
            )
            tab.setTypeface(null, if (tab === selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvJournalEntries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = journalAdapter
        }

        binding.rvGallery.apply {
            layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL)
            adapter = galleryAdapter
        }
    }

    private fun fetchJournals() {
        val entries = localJournalManager.getEntries().sortedByDescending { it.timestamp }

        if (isGalleryVisible) {
            val galleryEntries = entries.filter { it.imageUri != null }
            if (galleryEntries.isEmpty()) {
                binding.rvGallery.visibility = View.GONE
                binding.layoutGalleryEmpty.visibility = View.VISIBLE
            } else {
                binding.rvGallery.visibility = View.VISIBLE
                binding.layoutGalleryEmpty.visibility = View.GONE
                galleryAdapter.submitList(galleryEntries)
            }
        } else {
            if (entries.isEmpty()) {
                binding.rvJournalEntries.visibility = View.GONE
                binding.tvEmptyJournal.visibility = View.VISIBLE
            } else {
                binding.rvJournalEntries.visibility = View.VISIBLE
                binding.tvEmptyJournal.visibility = View.GONE

                val listItems = mutableListOf<JournalListItem>()

                val thisWeek = entries.filter { isWithinDays(it.timestamp.time, 7) }
                val thisMonth = entries.filter { !thisWeek.contains(it) && isWithinSameMonth(it.timestamp.time) }
                val earlierThisYear = entries.filter { !thisWeek.contains(it) && !thisMonth.contains(it) && isWithinSameYear(it.timestamp.time) }
                val earlier = entries.filter { !thisWeek.contains(it) && !thisMonth.contains(it) && !earlierThisYear.contains(it) }

                if (thisWeek.isNotEmpty()) {
                    listItems.add(JournalListItem.Header("This Week"))
                    listItems.addAll(thisWeek.map { JournalListItem.Entry(it) })
                }

                if (thisMonth.isNotEmpty()) {
                    listItems.add(JournalListItem.Header("This Month"))
                    listItems.addAll(thisMonth.map { JournalListItem.Entry(it) })
                }

                if (earlierThisYear.isNotEmpty()) {
                    listItems.add(JournalListItem.Header("Earlier this Year"))
                    listItems.addAll(earlierThisYear.map { JournalListItem.Entry(it) })
                }

                if (earlier.isNotEmpty()) {
                    listItems.add(JournalListItem.Header("Earlier"))
                    listItems.addAll(earlier.map { JournalListItem.Entry(it) })
                }

                journalAdapter.submitList(listItems)
            }
        }
    }

    private fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return timestamp >= cal.timeInMillis
    }

    private fun isWithinSameMonth(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        target.timeInMillis = timestamp
        return (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) &&
                (now.get(Calendar.MONTH) == target.get(Calendar.MONTH))
    }

    private fun isWithinSameYear(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        target.timeInMillis = timestamp
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}