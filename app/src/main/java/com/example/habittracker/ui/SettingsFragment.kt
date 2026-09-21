package com.example.habittracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.activity.LoginActivity
import com.example.habittracker.api.RetrofitClient
import com.example.habittracker.api.TokenManager
import com.example.habittracker.models.UserDto
import com.example.habittracker.models.UpdateProfileDto
import com.example.habittracker.databinding.FragmentSettingsBinding
import com.example.habittracker.util.SettingsManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var settingsManager: SettingsManager

    // Store profile locally to edit
    private var currentUserProfile: UserDto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        settingsManager = SettingsManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        updateThemeLabel(settingsManager.themeMode)
        setupSwitches()
        setupClickListeners()
    }

    private fun setupUserProfile() {
        val token = tokenManager.getToken()

        if (token.isNullOrEmpty()) {
            binding.profileNameTextView.text = "Guest User"
            binding.profileEmailTextView.text = "No active session"
            return
        }

        // Fetch user profile from API endpoint /api/users/me
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    currentUserProfile = user

                    val fullName = "${user.firstName} ${user.lastName}".trim()
                    binding.profileNameTextView.text = if (fullName.isNotEmpty()) fullName else "User"
                    binding.profileEmailTextView.text = user.email
                } else {
                    showToast("Failed to load user profile (${response.code()})")
                }
            } catch (e: Exception) {
                showToast("Network error while loading profile: ${e.message}")
            }
        }
    }

    private fun showEditProfileDialog() {
        val user = currentUserProfile ?: run {
            showToast("Profile data not loaded yet")
            return
        }

        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val firstNameInput = EditText(context).apply {
            hint = "First Name"
            setText(user.firstName)
        }
        val lastNameInput = EditText(context).apply {
            hint = "Last Name"
            setText(user.lastName)
        }

        layout.addView(firstNameInput)
        layout.addView(lastNameInput)

        AlertDialog.Builder(context)
            .setTitle("Edit Name")
            .setView(layout)
            .setPositiveButton("Save") { dialog, _ ->
                val newFirstName = firstNameInput.text.toString().trim()
                val newLastName = lastNameInput.text.toString().trim()

                if (newFirstName.isNotEmpty()) {
                    updateUserProfile(newFirstName, newLastName)
                } else {
                    showToast("First name cannot be empty")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserProfile(firstName: String, lastName: String) {
        lifecycleScope.launch {
            try {
                val updateDto = UpdateProfileDto(firstName = firstName, lastName = lastName)
                val response = RetrofitClient.getApiService(requireContext()).updateProfile(updateDto)

                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    currentUserProfile = updatedUser

                    binding.profileNameTextView.text = "${updatedUser.firstName} ${updatedUser.lastName}".trim()
                    showToast("Profile updated successfully")
                } else {
                    showToast("Failed to update profile (${response.code()})")
                }
            } catch (e: Exception) {
                showToast("Network error: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        // Allow clicking profile card/name to edit user details
        binding.profileNameTextView.setOnClickListener { showEditProfileDialog() }

        binding.themeOption.setOnClickListener { showThemeSelectionDialog() }
        binding.languageOption.setOnClickListener { showLanguageSelectionDialog() }
        binding.exportOption.setOnClickListener { exportUserData() }
        binding.importOption.setOnClickListener { importUserData() }
        binding.logoutOption.setOnClickListener { performLogout() }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val currentSelection = when (settingsManager.themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, currentSelection) { dialog, which ->
                val newMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                // Persist theme selection globally
                settingsManager.themeMode = newMode
                updateThemeLabel(newMode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateThemeLabel(mode: Int) {
        val label = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Light  >"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark  >"
            else -> "System  >"
        }
        binding.themeValueTextView.text = label
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Spanish", "French", "German")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val selectedLanguage = languages[which]
                binding.languageValueTextView.text = "$selectedLanguage  >"
                showToast("Language changed to $selectedLanguage")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportUserData() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Habitude Data Backup")
            putExtra(Intent.EXTRA_TEXT, "Exported habits data payload...")
        }
        startActivity(Intent.createChooser(shareIntent, "Export Data"))
    }

    private fun importUserData() {
        showToast("Importing habits data...")
    }

    private fun performLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                tokenManager.clearToken()
                showToast("Logged out successfully")
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupSwitches() {
        // Set initial toggle states
        binding.soundSwitch.isChecked = settingsManager.isSoundEnabled
        binding.hapticSwitch.isChecked = settingsManager.isHapticEnabled

        // Save state on change
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isSoundEnabled = isChecked
        }

        binding.hapticSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isHapticEnabled = isChecked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}