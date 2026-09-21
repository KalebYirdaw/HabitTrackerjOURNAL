package com.example.habittracker.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.habittracker.api.RetrofitClient
import com.example.habittracker.api.TokenManager
import com.example.habittracker.models.LoginRequest
import com.example.habittracker.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object Authenticated : AuthState() // Indicates user is already logged in
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val apiService = RetrofitClient.getApiService(application)
    private val tokenManager = TokenManager(application)

    // Initialize AuthRepository using your existing dependencies
    private val authRepository = AuthRepository(apiService, tokenManager)

    init {
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        val existingToken = tokenManager.getToken()
        if (!existingToken.isNullOrEmpty()) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, pass))
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    tokenManager.saveToken(responseBody.token)
                    _authState.value = AuthState.Success(responseBody.token)
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    // Fixes the unresolved reference error
    fun googleSignIn(context: Context, webClientId: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context, webClientId)

            result.fold(
                onSuccess = {
                    val savedToken = tokenManager.getToken().orEmpty()
                    _authState.value = AuthState.Success(savedToken)
                },
                onFailure = { throwable ->
                    _authState.value = AuthState.Error(
                        throwable.localizedMessage ?: "Google sign-in failed"
                    )
                }
            )
        }
    }
}