package com.example.habittracker.api

import android.content.Context
import com.example.habittracker.models.JournalEntry
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class LocalJournalManager(context: Context) {
    private val prefs = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    fun getEntries(): List<JournalEntry> {
        val json = prefs.getString("entries", null) ?: return emptyList()
        val type = object : TypeToken<List<JournalEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveEntry(entry: JournalEntry) {
        val entries = getEntries().toMutableList()
        entries.add(0, entry) // Add to top
        val json = gson.toJson(entries)
        prefs.edit().putString("entries", json).apply()
    }

    fun deleteEntry(id: String) {
        val entries = getEntries().toMutableList()
        entries.removeAll { it.id == id }
        val json = gson.toJson(entries)
        prefs.edit().putString("entries", json).apply()
    }
}