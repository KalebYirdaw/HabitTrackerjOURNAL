package com.example.habittracker.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.habittracker.databinding.ActivityHabitProgressBinding
import com.example.habittracker.models.Habit
import com.example.habittracker.viewModels.DetailUiState
import com.example.habittracker.viewModels.HabitDetailViewModel

class HabitProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitProgressBinding
    private val viewModel: HabitDetailViewModel by viewModels()

    private var currentHabit: Habit? = null
    private var valueRecorded: Double = 0.0
    private var isCompletedManual: Boolean = false

    // Launcher for receiving updated habit from EditHabitActivity
    private val editHabitLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            @Suppress("DEPRECATION")
            val updated = result.data?.getSerializableExtra("UPDATED_HABIT") as? Habit
            if (updated != null) {
                currentHabit = updated
                setupViews()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide default Action Bar to match custom TopBar layout
        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        currentHabit = intent.getSerializableExtra("EXTRA_HABIT") as? Habit

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        val habit = currentHabit ?: return
        binding.tvProgressTitle.text = habit.name

        val unit = habit.targetUnit ?: ""
        val target = habit.targetValue ?: 0.0

        // Target display formatted like "/10000 steps" or "/2000 ml"
        val formattedTarget = if (target % 1.0 == 0.0) target.toInt().toString() else target.toString()
        binding.tvProgressTarget.text = "/$formattedTarget $unit".trim()

        valueRecorded = habit.todayValueRecorded ?: 0.0
        updateDisplay()
    }

    private fun updateDisplay() {
        // Render current value display
        val formattedValue = if (valueRecorded % 1.0 == 0.0) valueRecorded.toInt().toString() else valueRecorded.toString()
        binding.tvValueDisplay.text = formattedValue

        val target = currentHabit?.targetValue ?: 0.0

        if (target > 0) {
            val rawPercentage = (valueRecorded / target) * 100
            val percentClamped = rawPercentage.coerceIn(0.0, 100.0).toInt()

            // Update circular progress bar
            binding.progressBar.progress = percentClamped
        } else {
            binding.progressBar.progress = 0
        }
    }

    private fun setupListeners() {

        // back button
        binding.btnBack.setOnClickListener { finish() }

        // current value button
        binding.tvValueDisplay.setOnClickListener {
            showInputValueDialog()
        }

        // more button
        binding.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener { menuItem ->
                // takes user to edit habit
                when (menuItem.title) {
                    // Launch Edit Screen
                    "Edit" -> {
                        val intent = Intent(this, EditHabitActivity::class.java).apply {
                            putExtra("EXTRA_HABIT", currentHabit)
                        }
                        startActivity(intent)
                        true
                    }
                    // open delete dialog
                    "Delete" -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // add btn
        binding.btnPlus.setOnClickListener {
            valueRecorded += 1.0
            updateDisplay()
        }

        // minus btn
        binding.btnMinus.setOnClickListener {
            valueRecorded = (valueRecorded - 1.0).coerceAtLeast(0.0)
            updateDisplay()
        }

        // reset btn
        binding.btnReset.setOnClickListener {
            valueRecorded = 0.0
            isCompletedManual = false
            updateDisplay()
        }

        // complete btn
        binding.btnComplete.setOnClickListener {
            val target = currentHabit?.targetValue ?: 0.0
            if (target > 0) {
                valueRecorded = target
            }
            isCompletedManual = true
            updateDisplay()
        }

        // save btn
        binding.btnSaveProgress.setOnClickListener {
            val id = currentHabit?.id ?: return@setOnClickListener
            val target = currentHabit?.targetValue ?: 0.0
            val isCompleted = isCompletedManual || (target > 0 && valueRecorded >= target)

            viewModel.recordProgress(id, isCompleted, valueRecorded)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DetailUiState.Loading -> {
                    binding.btnSaveProgress.isEnabled = false
                }
                is DetailUiState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()

                    currentHabit?.todayValueRecorded = valueRecorded

                    val target = currentHabit?.targetValue ?: 0.0
                    val isCompleted = isCompletedManual || (target > 0 && valueRecorded >= target)

                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_HABIT_ID", currentHabit?.id)
                        putExtra("UPDATED_VALUE", valueRecorded)
                        putExtra("IS_COMPLETED", isCompleted)
                        putExtra("EXTRA_HABIT", currentHabit)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is DetailUiState.Error -> {
                    binding.btnSaveProgress.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                DetailUiState.Idle -> {}
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Delete") { _, _ ->
                currentHabit?.id?.let { id ->
                    viewModel.deleteHabit(id) // Assumes deleteHabit in ViewModel
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInputValueDialog() {

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            // Pre-fill with current value
            val formattedValue = if (valueRecorded % 1.0 == 0.0) valueRecorded.toInt().toString() else valueRecorded.toString()
            setText(formattedValue)
            setSelection(text.length) // Place cursor at end
        }

        val container = FrameLayout(this).apply {
            val margin = (20 * resources.displayMetrics.density).toInt()
            setPadding(margin, (8 * resources.displayMetrics.density).toInt(), margin, 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Set Progress Value")
            .setMessage("Enter new value:")
            .setView(container)
            .setPositiveButton("Set") { _, _ ->
                val enteredString = input.text.toString().trim()
                val newValue = enteredString.toDoubleOrNull()
                if (newValue != null && newValue >= 0.0) {
                    valueRecorded = newValue
                    updateDisplay()
                } else {
                    Toast.makeText(this, "Please enter a valid positive number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}