package com.example.habittracker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Habit(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("targetValue") val targetValue: Double? = null,
    @SerializedName("targetUnit") val targetUnit: String? = null,
    @SerializedName("currentStreak") val currentStreak: Int = 0,
    @SerializedName("longestStreak") val longestStreak: Int = 0,
    @SerializedName("todayValueRecorded") var todayValueRecorded: Double? = 0.0,
    @SerializedName("isCompletedToday") var isCompletedToday: Boolean = false
) : Serializable