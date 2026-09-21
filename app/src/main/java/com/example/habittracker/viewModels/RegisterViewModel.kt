package com.example.habittracker.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.habittracker.api.RetrofitClient
import com.example.habittracker.api.TokenManager
import com.example.habittracker.models.RegisterRequest
import com.example.habittracker.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    // Fix 1: Explicit type parameter for MutableLiveData
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val apiService = RetrofitClient.getApiService(application)
    private val tokenManager = TokenManager(application)
    private val authRepository = AuthRepository(apiService, tokenManager)

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        pass: String,
        verifyPass: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (pass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        if (pass != verifyPass) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    email = email,
                    password = pass,
                    firstName = firstName,
                    lastName = lastName
                )
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    _authState.value = AuthState.Success("")
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    // Google SSO Registration/Sign-In
    fun googleSignIn(context: Context, webClientId: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(context, webClientId)
                    .onSuccess {
                        _authState.value = AuthState.Success("")
                    }
                    .onFailure { throwable ->
                        _authState.value = AuthState.Error(
                            throwable.localizedMessage ?: "Google sign-up failed"
                        )
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google sign-up failed")
            }
        }
    }
}