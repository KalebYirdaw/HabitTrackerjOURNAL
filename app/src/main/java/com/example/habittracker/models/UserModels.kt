package com.example.habittracker.models

data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class UpdateProfileDto(
    val firstName: String,
    val lastName: String
)
