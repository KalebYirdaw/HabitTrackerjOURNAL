package com.example.habittracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_user_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME_MODE = "key_theme_mode"
        const val KEY_SOUND_ENABLED = "key_sound_enabled"
        const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
    }

    // Save & Retrieve Theme Mode
    var themeMode: Int
        get() = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) {
            prefs.edit().putInt(KEY_THEME_MODE, value).apply()
            AppCompatDelegate.setDefaultNightMode(value)
        }

    // Save & Retrieve Sound Preference
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    // Save & Retrieve Haptic Preference
    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()
}