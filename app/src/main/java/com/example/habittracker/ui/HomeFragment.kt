package com.example.habittracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.activity.HabitProgressActivity
import com.example.habittracker.R
import com.example.habittracker.adapters.HabitAdapter
import com.example.habittracker.databinding.FragmentHomeBinding
import com.example.habittracker.util.FeedbackUtils
import com.example.habittracker.viewModels.HomeUiState
import com.example.habittracker.viewModels.HomeViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var feedbackUtils: FeedbackUtils? = null // sound & haptics


    // Displays the list of habit cards using HabitAdapter
    private val habitsAdapter = HabitAdapter(
        // opens habit progress activity
        onHabitClick = { habit ->
            // passes the selected habit object as an extra
            val intent = Intent(requireContext(), HabitProgressActivity::class.java).apply {
                putExtra("EXTRA_HABIT", habit)
            }
            startActivity(intent)
        },

        // marks habit as complete and triggers sound + haptic feedback
        onToggleComplete = { habit, isChecked, view ->
            if (isChecked) {
                // triggers haptic and sound via FeedbackUtils
                feedbackUtils?.triggerCompletionFeedback(view)
            }
            // notifies HomeViewModel to toggle the completion state.
            viewModel.toggleHabitCompletion(habit.id, isChecked)
        }
    )

    // stat calendar
    companion object {
        private const val ARG_SELECTED_DATE = "arg_selected_date"

        fun newInstance(selectedDate: Calendar): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle().apply {
                putSerializable(ARG_SELECTED_DATE, selectedDate)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        feedbackUtils = FeedbackUtils(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if a specific date was passed in
        val targetDate = arguments?.getSerializable(ARG_SELECTED_DATE) as? Calendar
        if (targetDate != null) {
            viewModel.fetchHabits(targetDate)
        }

        setupRecyclerView()
        observeViewModel()
        renderHomeHeader(viewModel.selectedCalendar)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchHabits()
    }

    // makes habit cards
    private fun setupRecyclerView() {
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitsAdapter
        }
    }

    // Observes viewModel.uiState and toggles visibility between progressBar, rvHabits, and tvError
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->

            when (state) {
                // displays loading screen
                is HomeUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvHabits.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }

                // display habits
                is HomeUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvHabits.visibility = View.VISIBLE
                    habitsAdapter.submitList(state.habits)
                    habitsAdapter.isInteractionEnabled = !state.isFuture
                    renderHomeHeader(state.selectedDate)
                }

                // displays error
                is HomeUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    // horizontal date strip
    private fun renderHomeHeader(selectedCal: Calendar) {

        binding.tvHomeSubtitle.text =
            android.text.format.DateFormat.format("d MMMM yyyy", selectedCal).toString().uppercase()

        val today = Calendar.getInstance()
        val container = binding.dateStripContainer
        container.removeAllViews()

        val startOffset = -3
        val dayCount = 14

        for (i in startOffset until startOffset + dayCount) {
            val cal = today.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, i)

            val isSelected = isSameDay(cal, selectedCal)
            val isToday = isSameDay(cal, today)

            val cellView = makeDateStripCell(cal, isSelected = isSelected, isToday = isToday)

            // Set Click Listener to switch date
            cellView.setOnClickListener {
                viewModel.fetchHabits(cal)
            }

            container.addView(cellView)
        }
    }

    private fun makeDateStripCell(cal: Calendar, isSelected: Boolean, isToday: Boolean): View {
        val cellLayout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                dpToPx(44),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(4)
                marginEnd = dpToPx(4)
            }
        }

        val dayAbbrev = android.text.format.DateFormat.format("EEE", cal).toString().uppercase()
        val dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString()

        cellLayout.addView(android.widget.TextView(requireContext()).apply {
            text = dayAbbrev
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            setTextColor(ContextCompat.getColor(requireContext(), R.color.stats_text_muted))
        })

        cellLayout.addView(android.widget.TextView(requireContext()).apply {
            text = dayNumber
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.white else R.color.black
                )
            )
            setBackgroundResource(
                if (isSelected) R.drawable.bg_day_circle_today else R.drawable.bg_day_circle
            )
            layoutParams = android.widget.LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)).apply {
                topMargin = dpToPx(4)
            }
        })

        return cellLayout
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        feedbackUtils?.release()
        feedbackUtils = null
        _binding = null
    }
}