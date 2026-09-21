package com.example.habittracker.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val requestBuilder = originalRequest.newBuilder()

        // attaches token to http header
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // forwards the updated request
        val response = chain.proceed(requestBuilder.build())

        // Automatically clear expired tokens on 401 Unauthorized
        if (response.code == 401) {
            tokenManager.clearToken()
        }

        return response
    }
}