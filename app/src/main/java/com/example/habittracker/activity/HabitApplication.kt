package com.example.habittracker.activity

import android.app.Application
import com.example.habittracker.util.SettingsManager

class HabitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply saved dark mode state globally on app start before ANY activity launches
        val settingsManager = SettingsManager(this)
        settingsManager.themeMode = settingsManager.themeMode
    }
}