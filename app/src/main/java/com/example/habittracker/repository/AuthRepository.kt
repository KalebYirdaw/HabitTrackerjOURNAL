package com.example.habittracker.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.habittracker.api.ApiService
import com.example.habittracker.api.TokenManager
import com.example.habittracker.models.SsoRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String
    ): Result<Unit> {
        return try {
            // uses Android’s official unified authentication API
            val credentialManager = CredentialManager.create(context)

            // configures the Google Sign-In prompt
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Allows users to pick any Google account on the device
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // displays the system bottom-sheet account picker to the user
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            // checks if the returned credential matches
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                // returns the OAuth idToken issued by Google
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // send Google Token to custom api via ApiService
                val apiResponse = apiService.ssoLogin(SsoRequest(idToken = idToken, provider = "Google"))

                if (apiResponse.isSuccessful && apiResponse.body() != null) {
                    // it returns JWT and stores it
                    val jwtToken = apiResponse.body()!!.token
                    tokenManager.saveToken(jwtToken)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Backend login failed: ${apiResponse.code()}"))
                }
            } else {
                Result.failure(Exception("Invalid credential returned from Google."))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}