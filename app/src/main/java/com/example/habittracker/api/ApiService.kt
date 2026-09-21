package com.example.habittracker.api

import com.example.habittracker.models.CreateHabitRequest
import com.example.habittracker.models.DetailedHabit
import com.example.habittracker.models.Habit
import com.example.habittracker.models.HabitCompletionRequest
import com.example.habittracker.models.LoginRequest
import com.example.habittracker.models.LoginResponse
import com.example.habittracker.models.RegisterRequest
import com.example.habittracker.models.RegisterResponse
import com.example.habittracker.models.SsoRequest
import com.example.habittracker.models.StatsOverview
import com.example.habittracker.models.UpdateProfileDto
import com.example.habittracker.models.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/* The custom Apis endpoints */
interface ApiService {
    /* Auth */

    // google single sign on
    @POST("api/auth/sso-login")
    suspend fun ssoLogin(@Body request: SsoRequest): Response<LoginResponse>
    // register user
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    // logs in user
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileDto): Response<UserDto>

    /* Habits */

    // returns all habits
    @GET("api/habits")
    suspend fun getHabits(@Query("date") date: String? = null): Response<List<Habit>>

    // returns specified habits
    @GET("api/habits/{id}")
    suspend fun getHabitById(@Path("id") id: String): Response<Habit>

    // creates a habit
    @POST("api/habits")
    suspend fun createHabit(@Body request: CreateHabitRequest): Response<Habit>

    // updates an existing habit
    @PUT("api/habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: String,
        @Body request: CreateHabitRequest,
    ): Response<Unit>

    // remove habit
    @DELETE("api/habits/{id}")
    suspend fun deleteHabit(
        @Path("id") id: String,
    ): Response<Unit>

    // logs complete habits
    @POST("api/habits/{id}/log")
    suspend fun completeHabit(
        @Path("id") id: String,
        @Body request: HabitCompletionRequest,
    ): Response<Habit>


    /* Stats */
    // returns overall statistics
    @GET("api/stats/overview")
    suspend fun getStatsOverview(): Response<StatsOverview>

    // returns an extended list of detailed habit stats
    @GET("api/stats/habits-detailed")
    suspend fun getDetailedHabits(): Response<List<DetailedHabit>>
}