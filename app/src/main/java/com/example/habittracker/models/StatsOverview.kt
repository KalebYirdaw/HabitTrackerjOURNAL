package com.example.habittracker.models

data class StatsOverview(
    val totalHabits: Int,
    val habitsDoneToday: Int,
    val activeHabits: Int,
    val bestStreak: Int,
    val weeklyCompletionRate: Double,
    val monthlyCompletionRate: Double,
    val yearlyCompletionRate: Double,
    val weeklyBarData: Map<String, Double>
)