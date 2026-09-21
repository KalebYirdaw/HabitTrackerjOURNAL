package com.example.habittracker.models

data class HabitCompletionHistory(
    val date: String,
    val valueRecorded: Double,
    val completed: Boolean
)