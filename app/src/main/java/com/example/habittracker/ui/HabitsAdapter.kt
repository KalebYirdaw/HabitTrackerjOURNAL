package com.example.habittracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemHabitBinding
import com.example.habittracker.models.Habit
import java.util.Locale

class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit, // clicking habit
    private val onToggleComplete: (Habit, Boolean, View) -> Unit // click on button (passes target View for feedback)
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    var isInteractionEnabled: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged() // Refresh UI state when interaction status changes
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {

        // Binds views directly to Kotlin variables
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {

            // display habit title and streak
            binding.tvHabitName.text = habit.name
            binding.tvStreak.text = "${habit.currentStreak} day streak"

            // Icon — matched from the habit name, same presets as the Add screen
            binding.ivHabitIcon.setImageResource(iconForHabit(habit.name))

            // Checkbox logic
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = habit.isCompletedToday

            binding.cbCompleted.setOnCheckedChangeListener { view, isChecked ->
                onToggleComplete(habit, isChecked, view)
            }

            // Progress Bar Calculation
            val currentValue = habit.todayValueRecorded ?: 0.0
            val targetValue = habit.targetValue ?: 0.0
            val unit = habit.targetUnit ?: ""

            // checks if target value is greater
            if (targetValue > 0) {

                // make progress visible
                binding.layoutProgress.visibility = View.VISIBLE

                // calculate percentage
                val progressPercentage = ((currentValue / targetValue) * 100).coerceIn(0.0, 100.0).toInt()
                binding.pbHabitProgress.progress = progressPercentage

                // Format text labels
                binding.tvProgressText.text = "$currentValue / $targetValue $unit".trim()
                binding.tvProgressPercent.text = String.format(Locale.US, "%d%%", progressPercentage)
            } else {
                binding.layoutProgress.visibility = View.GONE
            }

            // Click listener to open detail / progress logger
            binding.root.setOnClickListener {
                onHabitClick(habit)
            }
        }
    }

    // automatically assigns a matching preset icon drawable
    private fun iconForHabit(name: String): Int {
        val lower = name.lowercase(Locale.US)
        return when {
            lower.contains("walk") -> R.drawable.ic_walk
            lower.contains("run") -> R.drawable.ic_run
            lower.contains("cycle") || lower.contains("bike") -> R.drawable.ic_cycle
            lower.contains("read") -> R.drawable.ic_read
            lower.contains("sleep") -> R.drawable.ic_sleep
            lower.contains("workout") || lower.contains("gym") || lower.contains("exercise") -> R.drawable.ic_workout
            lower.contains("meditat") -> R.drawable.ic_meditate
            else -> R.drawable.ic_habit_default
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean =
            oldItem == newItem
    }
}