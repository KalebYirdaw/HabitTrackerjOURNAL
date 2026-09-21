package com.example.habittracker.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityLoginBinding
import com.example.habittracker.viewModels.AuthState
import com.example.habittracker.viewModels.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Email/Password Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // Google SSO Login
        binding.btnGoogleSignIn.setOnClickListener {
            val webClientId = "111626820867-apsg5m8tunta7aak709kjq39nogl6oob.apps.googleusercontent.com" // Replace with your Google Web Client ID
            viewModel.googleSignIn(context = this, webClientId = webClientId)
        }

        // Register Navigation
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        observeState()
    }

    private fun observeState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setUiEnabled(false)
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    setUiEnabled(true)
                    Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthState.Authenticated -> {
                    // User already has a valid token saved
                    navigateToMain()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    setUiEnabled(true)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    setUiEnabled(true)
                }
            }
        }
    }

    private fun setUiEnabled(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnGoogleSignIn.isEnabled = enabled
        binding.btnRegister.isEnabled = enabled
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}