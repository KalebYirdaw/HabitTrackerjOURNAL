package com.example.habittracker.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.api.LocalJournalManager
import com.example.habittracker.databinding.ActivityJournalDetailBinding
import com.example.habittracker.models.JournalEntry
import java.text.SimpleDateFormat
import java.util.Locale
import android.net.Uri
import android.view.View

class JournalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJournalDetailBinding
    private lateinit var localJournalManager: LocalJournalManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localJournalManager = LocalJournalManager(this)

        val entry = intent.getSerializableExtra("entry") as? JournalEntry
        if (entry == null) {
            finish()
            return
        }

        displayEntry(entry)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDeleteEntry.setOnClickListener {
            localJournalManager.deleteEntry(entry.id)
            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun displayEntry(entry: JournalEntry) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault())
        binding.tvDetailTitle.text = entry.title
        binding.tvDetailDate.text = dateFormat.format(entry.timestamp)
        binding.tvDetailContent.text = entry.content

        entry.imageUri?.let { uriString ->
            binding.cardDetailImage.visibility = View.VISIBLE
            binding.ivDetailImage.setImageURI(Uri.parse(uriString))
        } ?: run {
            binding.cardDetailImage.visibility = View.GONE
        }
    }
}