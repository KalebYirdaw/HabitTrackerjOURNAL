package com.example.habittracker.models

import com.google.gson.annotations.SerializedName

data class CreateHabitRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("targetValue") val targetValue: Double? = null,
    @SerializedName("targetUnit") val targetUnit: String? = null
)