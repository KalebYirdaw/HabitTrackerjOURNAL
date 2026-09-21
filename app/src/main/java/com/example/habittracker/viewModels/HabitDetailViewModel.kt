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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed class DetailUiState {
    object Idle : DetailUiState()
    object Loading : DetailUiState()
    data class Success(val message: String) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class HabitDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)
    private val _uiState = MutableLiveData<DetailUiState>(DetailUiState.Idle)
    val uiState: LiveData<DetailUiState> = _uiState

    // Updates the habit
    fun updateHabit(habit: Habit) {
        // sets to loading
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                // map Habit instance to CreateHabitRequest payload
                val request = CreateHabitRequest(
                    name = habit.name,
                    description = habit.description,
                    targetValue = habit.targetValue,
                    targetUnit = habit.targetUnit
                )
                // send payload to api
                val response = apiService.updateHabit(habit.id, request)
                if (response.isSuccessful) {
                    _uiState.value = DetailUiState.Success("Habit updated successfully")
                } else {
                    _uiState.value = DetailUiState.Error("Failed to update habit: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // Removes the habit
    fun deleteHabit(habitId: String) {
        // sets to loading
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                // removes habits using api
                val response = apiService.deleteHabit(habitId)
                if (response.isSuccessful) {
                    _uiState.value = DetailUiState.Success("Habit deleted successfully")
                } else {
                    _uiState.value = DetailUiState.Error("Failed to delete habit: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // Formats date to YYYY-MM-DD matching the DateOnly API field requirement
    fun recordProgress(habitId: String, isCompleted: Boolean, valueRecorded: Double) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                // creates log payload
                val request = HabitCompletionRequest(
                    date = sdf.format(Date()),
                    completed = isCompleted,
                    valueRecorded = valueRecorded
                )

                // sends payload to api
                val response = apiService.completeHabit(habitId, request)
                if (response.isSuccessful) {
                    _uiState.value = DetailUiState.Success("Progress recorded")
                } else {
                    _uiState.value = DetailUiState.Error("Failed to save progress: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}