package com.example.habittracker.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityEditHabitBinding
import com.example.habittracker.models.Habit
import com.example.habittracker.viewModels.DetailUiState
import com.example.habittracker.viewModels.HabitDetailViewModel

class EditHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHabitBinding
    private val viewModel: HabitDetailViewModel by viewModels()
    private var currentHabit: Habit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        currentHabit = intent.getSerializableExtra("EXTRA_HABIT") as? Habit

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        val habit = currentHabit ?: return

        binding.etDetailName.setText(habit.name)
        binding.etDetailDescription.setText(habit.description ?: "")

        habit.targetValue?.let { value ->
            val formattedValue = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            binding.etDetailTargetValue.setText(formattedValue)
        }

        binding.etDetailTargetUnit.setText(habit.targetUnit ?: "")
    }

    private fun setupListeners() {
        binding.btnEditBack.setOnClickListener {
            finish()
        }

        binding.btnUpdateHabit.setOnClickListener {
            saveHabitChanges()
        }
    }

    private fun saveHabitChanges() {
        val habit = currentHabit ?: return
        val name = binding.etDetailName.text.toString().trim()
        val description = binding.etDetailDescription.text.toString().trim().ifEmpty { null }
        val targetValueStr = binding.etDetailTargetValue.text.toString().trim()
        val targetUnit = binding.etDetailTargetUnit.text.toString().trim().ifEmpty { null }

        if (name.isEmpty()) {
            binding.etDetailName.error = "Name is required"
            return
        }

        val updatedHabit = habit.copy(
            name = name,
            description = description,
            targetValue = targetValueStr.toDoubleOrNull(),
            targetUnit = targetUnit
        )

        currentHabit = updatedHabit
        viewModel.updateHabit(updatedHabit)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DetailUiState.Loading -> {
                    binding.btnUpdateHabit.isEnabled = false
                }
                is DetailUiState.Success -> {
                    Toast.makeText(this, "Habit updated successfully", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_HABIT", currentHabit)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is DetailUiState.Error -> {
                    binding.btnUpdateHabit.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                DetailUiState.Idle -> {}
            }
        }
    }
}