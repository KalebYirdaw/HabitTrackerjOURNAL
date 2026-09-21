package com.example.habittracker.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.habittracker.R

class FeedbackUtils(private val context: Context) {

    private val settingsManager = SettingsManager(context)
    private var soundPool: SoundPool? = null
    private var completionSoundId: Int = 0
    private var isSoundLoaded = false

    init { initSoundPool() }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Listener to verify the sound file is fully loaded into memory before playing
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                isSoundLoaded = true
                Log.d("FeedbackUtils", "Sound loaded successfully with ID: $sampleId")
            } else {
                Log.e("FeedbackUtils", "Error loading sound file, status: $status")
            }
        }

        try {
            completionSoundId = soundPool?.load(context, R.raw.completion_sound, 1) ?: 0
        } catch (e: Exception) {
            Log.e("FeedbackUtils", "Failed to load raw audio resource: ${e.message}")
        }
    }

    fun triggerCompletionFeedback(view: View) {
        // Haptic Feedback
        if (settingsManager.isHapticEnabled) {
            performHaptic(view)
        }

        // Sound Effect
        if (settingsManager.isSoundEnabled && isSoundLoaded && completionSoundId != 0) {
            soundPool?.play(completionSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun performHaptic(view: View) {
        // Step A: Attempt View-based Haptic Feedback with IGNORE_GLOBAL_SETTING flag
        val performFlags = HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        val performed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM, performFlags)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, performFlags)
        }

        // Step B: Fallback to System Vibrator directly if View haptics fail (Crucial for Samsung A13)
        if (!performed) {
            vibrateDirectly()
        }
    }

    private fun vibrateDirectly() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}