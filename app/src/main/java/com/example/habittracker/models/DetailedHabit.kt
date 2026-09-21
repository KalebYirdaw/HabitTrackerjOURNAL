package com.example.habittracker.models

data class DetailedHabit(
    val id: String,
    val name: String,
    val description: String?,
    val targetValue: Double?,
    val targetUnit: String?,
    val currentStreak: Int,
    val longestStreak: Int,
    val todayValueRecorded: Double,
    val isCompletedToday: Boolean,
    val history: List<HabitCompletionHistory>
)