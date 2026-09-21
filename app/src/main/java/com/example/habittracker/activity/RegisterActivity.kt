package com.example.habittracker.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityRegisterBinding
import com.example.habittracker.viewModels.AuthState
import com.example.habittracker.viewModels.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    // Web Client ID used for Google Credential Manager
    private val webClientId = "111626820867-apsg5m8tunta7aak709kjq39nogl6oob.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val verifyPassword = binding.verifyPasswordEditText.text.toString()

            viewModel.register(firstName, lastName, email, phone, password, verifyPassword)
        }

        // Google SSO Register / Sign-In
        binding.btnGoogleSignUp.setOnClickListener {
            viewModel.googleSignIn(context = this, webClientId = webClientId)
        }

        binding.loginNavigationButton.setOnClickListener {
            finish()
        }
    }

    private fun observeState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                }
                else -> {}
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}