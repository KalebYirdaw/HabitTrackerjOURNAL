package com.example.habittracker.models

import java.io.Serializable
import java.util.Date
import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Date = Date(),
    val imageUri: String? = null
) : Serializable