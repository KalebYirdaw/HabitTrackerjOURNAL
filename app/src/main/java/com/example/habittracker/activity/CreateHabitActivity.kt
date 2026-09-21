package com.example.habittracker.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityCreateHabitBinding
import com.example.habittracker.viewModels.HomeViewModel

class CreateHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateHabitBinding
    private val viewModel: HomeViewModel by viewModels()
    private var isFormState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupListeners()
        setupDropdowns()
        setupBottomNav()
    }

    private fun setupBottomNav() {
        // Mark the Add tab as the active one on this screen
        findViewById<ImageView>(com.example.habittracker.R.id.icon_add)
            ?.setImageResource(com.example.habittracker.R.drawable.ic_add_bold)

        findViewById<LinearLayout>(com.example.habittracker.R.id.nav_habits).setOnClickListener {
            goToMainTab(MainActivity.TAB_HABITS)
        }
        findViewById<LinearLayout>(com.example.habittracker.R.id.nav_stats).setOnClickListener {
            goToMainTab(MainActivity.TAB_STATS)
        }
        findViewById<LinearLayout>(com.example.habittracker.R.id.nav_journal).setOnClickListener {
            goToMainTab(MainActivity.TAB_JOURNAL)
        }
        findViewById<LinearLayout>(com.example.habittracker.R.id.nav_settings).setOnClickListener {
            goToMainTab(MainActivity.TAB_SETTINGS)
        }
    }

    private fun goToMainTab(tab: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TAB, tab)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun setupDropdowns() {
        val frequencies = arrayOf("Daily", "Weekly", "Monthly")
        val freqAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, frequencies)
        binding.etNewHabitFrequency.setAdapter(freqAdapter)
    }

    private fun setupListeners() {

        // Vertical List Rows Click Listeners
        binding.cardWalk.setOnClickListener { openConfigurationForm("Walk") }
        binding.cardRun.setOnClickListener { openConfigurationForm("Run") }
        binding.cardCycle.setOnClickListener { openConfigurationForm("Cycle") }
        binding.cardRead.setOnClickListener { openConfigurationForm("Read Book") }
        binding.cardSleep.setOnClickListener { openConfigurationForm("Sleep") }
        binding.cardWorkout.setOnClickListener { openConfigurationForm("Workout") }
        binding.cardMeditate.setOnClickListener { openConfigurationForm("Meditate") }
        binding.cardCustomHabit.setOnClickListener { openConfigurationForm("") }

        binding.btnSaveNewHabit.setOnClickListener {
            saveHabitToDashboard()
        }
    }

    private fun openConfigurationForm(habitName: String) {
        isFormState = true
        binding.layoutHabitSelection.visibility = View.GONE
        binding.layoutHabitConfigForm.visibility = View.VISIBLE
        binding.tvScreenTitle.text = if (habitName.isEmpty()) "Custom Habit" else "Configure $habitName"

        binding.etNewHabitName.setText(habitName)

        // Pre-fill parameters based on preset type
        when (habitName) {
            "Walk" -> {
                binding.etNewHabitDescription.setText("Daily walking for cardio fitness")
                binding.etNewHabitTargetValue.setText("6000")
                binding.etNewHabitTargetUnit.setText("Steps")
            }
            "Run" -> {
                binding.etNewHabitDescription.setText("Running routines")
                binding.etNewHabitTargetValue.setText("5")
                binding.etNewHabitTargetUnit.setText("km")
            }
            "Cycle" -> {
                binding.etNewHabitDescription.setText("Cycling for fitness")
                binding.etNewHabitTargetValue.setText("10")
                binding.etNewHabitTargetUnit.setText("km")
            }
            "Read Book" -> {
                binding.etNewHabitDescription.setText("Reading for educational progress")
                binding.etNewHabitTargetValue.setText("20")
                binding.etNewHabitTargetUnit.setText("Pages")
            }
            "Sleep" -> {
                binding.etNewHabitDescription.setText("Consistent restful sleep cycle")
                binding.etNewHabitTargetValue.setText("8")
                binding.etNewHabitTargetUnit.setText("Hours")
            }
            "Workout" -> {
                binding.etNewHabitDescription.setText("Strength training and exercise")
                binding.etNewHabitTargetValue.setText("45")
                binding.etNewHabitTargetUnit.setText("mins")
            }
            "Meditate" -> {
                binding.etNewHabitDescription.setText("Mindfulness and deep breathing")
                binding.etNewHabitTargetValue.setText("15")
                binding.etNewHabitTargetUnit.setText("mins")
            }
        }
    }


    private fun saveHabitToDashboard() {
        val name = binding.etNewHabitName.text.toString().trim()
        val description = binding.etNewHabitDescription.text.toString().trim().ifEmpty { null }
        val targetValueStr = binding.etNewHabitTargetValue.text.toString().trim()
        val targetUnit = binding.etNewHabitTargetUnit.text.toString().trim().ifEmpty { null }
        // Frequency is captured in the form for future use, but the current
        // HomeViewModel.createHabit()/backend model doesn't have a frequency
        // field yet, so it isn't sent with the request.

        if (name.isEmpty()) {
            binding.etNewHabitName.error = "Name is required"
            return
        }

        val targetValue = targetValueStr.toDoubleOrNull()

        viewModel.createHabit(
            title = name,
            description = description,
            targetValue = targetValue,
            targetUnit = targetUnit
        )

        Toast.makeText(this, "Habit saved to Dashboard", Toast.LENGTH_SHORT).show()
        finish()
    }
}