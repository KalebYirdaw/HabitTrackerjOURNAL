package com.example.habittracker.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // custom api url
    private const val BASE_URL = "https://habittrackerapi20260915152205-edcjg0h7fdhfa3cc.southafricanorth-01.azurewebsites.net/"

    // ensures changes made to the instance variable by one thread are immediately visible to all other threads.
    @Volatile
    private var instance: ApiService? = null

    fun getApiService(context: Context): ApiService {
        return instance ?: synchronized(this) { // buildRetrofit is called only once,
            instance ?: buildRetrofit(context.applicationContext).also { instance = it }
        }
    }

    private fun buildRetrofit(context: Context): ApiService {

        // Manages access tokens
        val tokenManager = TokenManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager)) // attaches authentication headers
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}