package com.example.habittracker.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {


    // manages the hardware-backed root key stored inside the Android Keystore system.
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // both keys and values are encrypted at rest before being written to disk
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Used to encrypt preference keys deterministically.
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Used to encrypt preference values nondeterministically
    )

    // encrypts and writes the JWT string to storage
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    // reads and decrypts the stored JWT
    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    // removes the JWT key on user logout,
    fun clearToken() {
        prefs.edit().remove(KEY_JWT_TOKEN).apply()
    }

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
    }
}