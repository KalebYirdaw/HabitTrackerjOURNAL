package com.example.habittracker.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.habittracker.api.RetrofitClient
import com.example.habittracker.models.CreateHabitRequest
import com.example.habittracker.models.Habit
import com.example.habittracker.models.HabitCompletionRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val habits: List<Habit>, val selectedDate: Calendar, val isFuture: Boolean) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    // Track current selected date (defaults to today)
    var selectedCalendar: Calendar = Calendar.getInstance()
        private set

    private val apiService = RetrofitClient.getApiService(application)

    // returns all habits
    fun fetchHabits(cal: Calendar = selectedCalendar) {
        selectedCalendar = cal
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            try {
                val dateStr = formatDateForApi(cal.time)
                val response = apiService.getHabits(date = dateStr)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HomeUiState.Success(response.body()!!, selectedCalendar, isFuture = isFutureDate(selectedCalendar))
                } else {
                    _uiState.value = HomeUiState.Error("Failed to load habits: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    // creates habits
    fun createHabit(
        title: String,
        description: String?,
        targetValue: Double?,
        targetUnit: String?
    ) {
        viewModelScope.launch {
            try {
                // creates habit
                val request = CreateHabitRequest(
                    name = title,
                    description = description,
                    targetValue = targetValue,
                    targetUnit = targetUnit
                )

                // send payload to api
                val response = apiService.createHabit(request)

                if (response.isSuccessful) {
                    fetchHabits() // Refresh the habit list on successful creation
                } else {
                    _uiState.value = HomeUiState.Error("Failed to create habit: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to connect to server")
            }
        }
    }

    // button to complete habit
    fun toggleHabitCompletion(habitId: String, isCompleted: Boolean) {
        if (isFutureDate(selectedCalendar)) {
            _uiState.value = HomeUiState.Error("Cannot record progress for future dates")
            fetchHabits(selectedCalendar)
            return
        }
        viewModelScope.launch {
            // updates ui state
            val currentState = _uiState.value

            // if ui state is success
            if (currentState is HomeUiState.Success) {
                val updatedList = currentState.habits.map { habit ->
                    if (habit.id == habitId) {
                        val targetVal = habit.targetValue ?: 1.0
                        val newVal = if (isCompleted) targetVal else 0.0
                        habit.copy(
                            isCompletedToday = isCompleted,
                            todayValueRecorded = newVal
                        )
                    } else habit
                }

                _uiState.value = HomeUiState.Success(updatedList, selectedCalendar, isFuture = isFutureDate(selectedCalendar))

                try {
                    val targetHabit = currentState.habits.find { it.id == habitId }
                    val targetVal = targetHabit?.targetValue ?: 1.0
                    val recordedVal = if (isCompleted) targetVal else 0.0

                    // create habit complete log
                    val request = HabitCompletionRequest(
                        date = formatDateForApi(selectedCalendar.time),
                        completed = isCompleted,
                        valueRecorded = recordedVal
                    )

                    // send payload to api
                    val response = apiService.completeHabit(habitId, request)
                    if (!response.isSuccessful) {
                        fetchHabits()
                    }
                } catch (e: Exception) {
                    fetchHabits()
                }
            }
        }
    }

    fun recordHabitValue(habitId: UUID, valueRecorded: Double) {
        if (isFutureDate(selectedCalendar)) {
            _uiState.value = HomeUiState.Error("Cannot record progress for future dates")
            return
        }
        viewModelScope.launch {
            try {
                val request = HabitCompletionRequest(
                    date = getTodayDateOnlyString(),
                    completed = valueRecorded > 0,
                    valueRecorded = valueRecorded
                )

                val response = apiService.completeHabit(habitId.toString(), request)
                if (response.isSuccessful) {
                    fetchHabits()
                } else {
                    _uiState.value = HomeUiState.Error("Failed to record value: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to connect to server")
            }
        }
    }

    private fun formatDateForApi(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return dateFormat.format(date)
    }

    // Helper method to check if the calendar instance is strictly after today
    private fun isFutureDate(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        if (cal.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true
        if (cal.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return false
        return cal.get(Calendar.DAY_OF_YEAR) > today.get(Calendar.DAY_OF_YEAR)
    }

    private fun getTodayDateOnlyString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return dateFormat.format(Date())
    }
}