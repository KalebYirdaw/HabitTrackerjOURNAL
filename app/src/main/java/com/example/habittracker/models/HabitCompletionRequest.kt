package com.example.habittracker.models

import com.google.gson.annotations.SerializedName

data class HabitCompletionRequest(
    @SerializedName("date") val date: String,
    @SerializedName("completed") val completed: Boolean,
    @SerializedName("valueRecorded") val valueRecorded: Double? = null
)