package com.example.habittracker.models

/* AUTH ENDPOINTS */

/* Login */
data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val token: String,
    val expiration: String
)

/* SSO */
data class SsoRequest(
    val idToken: String,
    val provider: String = "Google"
)

/* Register */
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
data class RegisterResponse(
    val message: String
)