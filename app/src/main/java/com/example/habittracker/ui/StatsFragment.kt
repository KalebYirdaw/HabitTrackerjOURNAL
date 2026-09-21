package com.example.habittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentStatsBinding
import com.example.habittracker.models.Habit
import com.example.habittracker.viewModels.HomeUiState
import com.example.habittracker.viewModels.HomeViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private var habits: List<Habit> = emptyList()
    private val calendarMonth: Calendar = Calendar.getInstance()
    private val habitCardColors = listOf(
        R.color.stats_orange,
        R.color.stats_green,
        R.color.stats_purple,
        R.color.stats_blue
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCharts()
        showWeek() // default tab
        renderWeekHabitRows()
        renderMonthCalendar()
        setupStatLabels()
        renderMonthHabitCards()
        renderYearHabitCards()
        observeViewModel()


        binding.viewStatsMonth.ivPrevMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH, -1)
            renderMonthCalendar()
            updateMonthSubtitle()
        }
        binding.viewStatsMonth.ivNextMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH, 1)
            renderMonthCalendar()
            updateMonthSubtitle()
        }

 83220e4
        binding.tabWeek.setOnClickListener { showWeek() }
        binding.tabMonth.setOnClickListener { showMonth() }
        binding.tabYear.setOnClickListener { showYear() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchHabits()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state is HomeUiState.Success) {
                habits = state.habits
                setupStatLabels()
                setupCharts()
                renderWeekHabitRows()
                renderMonthHabitCards()
                renderYearHabitCards()
            }
        }
    }


    private fun showWeek() {
        binding.viewStatsWeek.root.visibility = View.VISIBLE
        binding.viewStatsMonth.root.visibility = View.GONE
        binding.viewStatsYear.root.visibility = View.GONE
        setActiveTab(binding.tabWeek)
        binding.tvStatsSubtitle.text =
            android.text.format.DateFormat.format("d MMM yyyy", Calendar.getInstance())
                .toString().uppercase()
    }


    private fun showMonth() {
        binding.viewStatsWeek.root.visibility = View.GONE
        binding.viewStatsMonth.root.visibility = View.VISIBLE
        binding.viewStatsYear.root.visibility = View.GONE
        setActiveTab(binding.tabMonth)
        updateMonthSubtitle()
    }


    private fun showYear() {
        binding.viewStatsWeek.root.visibility = View.GONE
        binding.viewStatsMonth.root.visibility = View.GONE
        binding.viewStatsYear.root.visibility = View.VISIBLE
        setActiveTab(binding.tabYear)
        binding.tvStatsSubtitle.text = Calendar.getInstance().get(Calendar.YEAR).toString()
    }

    private fun updateMonthSubtitle() {
        binding.tvStatsSubtitle.text =
            android.text.format.DateFormat.format("MMMM yyyy", calendarMonth)
                .toString().uppercase()
    }


    private fun setActiveTab(selected: TextView) {
        val tabs = listOf(binding.tabWeek, binding.tabMonth, binding.tabYear)
        for (tab in tabs) {
            tab.background = if (tab === selected)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            tab.setTextColor(
                ContextCompat.getColor(requireContext(), if (tab === selected) R.color.white else R.color.black)
            )
        }
    }

    private fun renderWeekHabitRows() {
        val container = binding.viewStatsWeek.habitHeatmapRows
        container.removeAllViews()

        val currentWeekCalendar = Calendar.getInstance()
        val currentDayIndex = (currentWeekCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

        habits.forEachIndexed { index, habit ->
            val accentColorRes = habitCardColors[index % habitCardColors.size]

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(6) }
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            row.addView(TextView(requireContext()).apply {
                text = habit.name
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(dpToPx(70), LinearLayout.LayoutParams.WRAP_CONTENT)
            })

            repeat(7) { dayIndex ->
                val isTodayCell = dayIndex == currentDayIndex

                // Calculate the date corresponding to this day column
                val cellCal = currentWeekCalendar.clone() as Calendar
                val dayOffset = dayIndex - currentDayIndex
                cellCal.add(Calendar.DAY_OF_YEAR, dayOffset)

                val cell = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(18), 1f).apply {
                        marginStart = dpToPx(3)
                        marginEnd = dpToPx(3)
                    }
                    if (isTodayCell && habit.isCompletedToday) {
                        setBackgroundColor(ContextCompat.getColor(requireContext(), accentColorRes))
                    } else {
                        setBackgroundResource(R.drawable.bg_heatmap_cell_empty)
                    }

                    // Allow user to tap week cell to open that day on Home Screen
                    setOnClickListener {
                        (activity as? com.example.habittracker.activity.MainActivity)
                            ?.navigateToHomeWithDate(cellCal)
                    }
                }
                row.addView(cell)
            }

            container.addView(row)
        }
    }

    private fun getWeekDatesForCurrentWeek(): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val diff = (7 + (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY)) % 7
        cal.add(Calendar.DAY_OF_MONTH, -diff)

        return (0..6).map {
            val dateStr = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            dateStr
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun setupCharts() {
        val habitsDoneToday = habits.count { it.isCompletedToday }
        val totalHabits = habits.size

        setupRateDonut(binding.viewStatsWeek.pieChartWeek, binding.viewStatsWeek.tvWeeklyRate, "Weekly Rate", habitsDoneToday, totalHabits)
        setupRateDonut(binding.viewStatsMonth.pieChartMonth, binding.viewStatsMonth.tvMonthlyRate, "Monthly Rate", habitsDoneToday, totalHabits)
        setupRateDonut(binding.viewStatsYear.pieChartYear, binding.viewStatsYear.tvYearlyRate, "Yearly Rate", habitsDoneToday, totalHabits)

        setupWeekBarChart(habitsDoneToday, totalHabits)
        setupYearBarChart(habitsDoneToday, totalHabits)
    }

    private fun setupRateDonut(
        pieChart: PieChart,
        rateLabel: TextView,
        ratePrefix: String,
        completedCount: Int,
        totalCount: Int
    ) {
        val remaining = (totalCount - completedCount).coerceAtLeast(0)
        val entries = if (totalCount == 0) {
            listOf(PieEntry(1f, "No habits"))
        } else {
            listOf(
                PieEntry(completedCount.toFloat(), "Done"),
                PieEntry(remaining.toFloat(), "Remaining")
            )
        }
        val sliceColors = if (totalCount == 0) {
            listOf(ContextCompat.getColor(requireContext(), R.color.stats_bar_light))
        } else {
            listOf(
                ContextCompat.getColor(requireContext(), R.color.stats_purple),
                ContextCompat.getColor(requireContext(), R.color.stats_bar_light)
            )
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = sliceColors
            setDrawValues(false)
        }
        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            holeRadius = 65f
            setTouchEnabled(false)
            invalidate()
        }
        val percent = if (totalCount == 0) 0 else (completedCount * 100) / totalCount
        rateLabel.text = "$ratePrefix  •  $percent%"
    }

    private fun makeCapsuleColumn(label: String, fillFraction: Float, trackHeightDp: Int = 130, barWidthDp: Int = 18): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val frame = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(barWidthDp), dpToPx(trackHeightDp))
        }

        val track = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_capsule_pill)?.mutate()?.apply {
                setTint(ContextCompat.getColor(requireContext(), R.color.stats_bar_light))
            }
        }
        frame.addView(track)

        val clampedFraction = fillFraction.coerceIn(0f, 1f)
        if (clampedFraction > 0f) {
            val fillHeightPx = (dpToPx(trackHeightDp) * clampedFraction).toInt().coerceAtLeast(dpToPx(barWidthDp))
            val fill = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, fillHeightPx
                ).apply { gravity = android.view.Gravity.BOTTOM }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_capsule_pill)?.mutate()?.apply {
                    setTint(ContextCompat.getColor(requireContext(), R.color.stats_bar_dark))
                }
            }
            frame.addView(fill)
        }

        column.addView(frame)

        column.addView(TextView(requireContext()).apply {
            text = label
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            setTextColor(ContextCompat.getColor(requireContext(), R.color.stats_text_muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(6) }
        })

        return column
    }

    private fun renderCapsuleBarRow(container: LinearLayout, labels: List<String>, highlightIndex: Int, highlightFraction: Float) {
        container.removeAllViews()
        labels.forEachIndexed { index, label ->
            val fraction = if (index == highlightIndex) highlightFraction else 0f
            container.addView(makeCapsuleColumn(label, fraction))
        }
    }

    private fun setupWeekBarChart(habitsDoneToday: Int, totalHabits: Int) {
        val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        val fraction = if (totalHabits == 0) 0f else habitsDoneToday.toFloat() / totalHabits
        renderCapsuleBarRow(binding.viewStatsWeek.weekBarChartRow, dayLabels, todayIndex, fraction)
    }

    private fun setupYearBarChart(habitsDoneToday: Int, totalHabits: Int) {
        val months = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        val fraction = if (totalHabits == 0) 0f else habitsDoneToday.toFloat() / totalHabits
        renderCapsuleBarRow(binding.viewStatsYear.yearBarChartRow, months, currentMonthIndex, fraction)
    }

    private fun renderMonthCalendar() {
        val grid = binding.viewStatsMonth.calendarGridMonth
        grid.removeAllViews()

        val monthCal = calendarMonth.clone() as Calendar
        monthCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)
        val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        // Empty offset cells before day 1
        repeat(firstDayOfWeek - 1) {
            grid.addView(makeDayCell(null, isToday = false, dateCal = null))
        }

        // Actual day cells
        for (day in 1..daysInMonth) {
            val isToday = isCurrentMonth && day == todayDayOfMonth

            // Construct Calendar instance for this exact cell date
            val cellCal = monthCal.clone() as Calendar
            cellCal.set(Calendar.DAY_OF_MONTH, day)

            grid.addView(makeDayCell(day, isToday, cellCal))
        }

        val monthLabel = android.text.format.DateFormat.format("MMMM yyyy", monthCal).toString().uppercase()
        binding.viewStatsMonth.tvCalendarMonthLabel.text = monthLabel
    }

    private fun makeDayCell(day: Int?, isToday: Boolean, dateCal: Calendar?): TextView {
        return TextView(requireContext()).apply {
            text = day?.toString() ?: ""
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isToday) R.color.white else R.color.black
                )
            )
            if (day != null) {
                setBackgroundResource(if (isToday) R.drawable.bg_day_circle_today else R.drawable.bg_day_circle)

                // Set click listener to navigate to HomeFragment for this date
                setOnClickListener {
                    dateCal?.let { targetDate ->
                        (activity as? com.example.habittracker.activity.MainActivity)
                            ?.navigateToHomeWithDate(targetDate)
                    }
                }
            }
            layoutParams = GridLayout.LayoutParams().apply {
                width = dpToPx(36)
                height = dpToPx(36)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                (this as MarginLayoutParams).setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
            }
        }
    }

    private fun setupStatLabels() {
        val bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
        val habitsDoneToday = habits.count { it.isCompletedToday }
        val totalHabits = habits.size
        val activeHabits = habits.count { !it.isCompletedToday }

        val weekStats = binding.viewStatsWeek
        bindStat(weekStats.statBestStreak, bestStreak.toString(), "Best Streaks", null)
        bindStat(weekStats.statPerfectDays, totalHabits.toString(), "Total Habits", null)
        bindStat(weekStats.statHabitsDone, habitsDoneToday.toString(), "Habits Done", null)
        bindStat(weekStats.statDailyAverage, activeHabits.toString(), "Active Habits", null)

        val monthStats = binding.viewStatsMonth
        bindStat(monthStats.statBestStreak, bestStreak.toString(), "Best Streaks", null)
        bindStat(monthStats.statPerfectDays, totalHabits.toString(), "Total Habits", null)
        bindStat(monthStats.statHabitsDone, habitsDoneToday.toString(), "Habits Done", null)
        bindStat(monthStats.statDailyAverage, activeHabits.toString(), "Active Habits", null)

        val yearStats = binding.viewStatsYear
        bindStat(yearStats.statBestStreak, bestStreak.toString(), "Best Streaks", null)
        bindStat(yearStats.statPerfectDays, totalHabits.toString(), "Total Habits", null)
        bindStat(yearStats.statHabitsDone, habitsDoneToday.toString(), "Habits Done", null)
        bindStat(yearStats.statDailyAverage, activeHabits.toString(), "Active Habits", null)
    }

    private fun bindStat(
        stat: com.example.habittracker.databinding.ItemStatBinding,
        value: String,
        label: String,
        unit: String?
    ) {
        stat.tvStatValue.text = if (unit != null) "$value $unit" else value
        stat.tvStatLabel.text = label
    }

    private fun renderMonthHabitCards() {
        val container = binding.viewStatsMonth.habitHeatmapCardsMonth
        container.removeAllViews()

        habits.forEachIndexed { index, habit ->
            container.addView(
                makeHabitHeatmapCard(habit, habitCardColors[index % habitCardColors.size])
            )
        }
    }

    private fun makeHabitHeatmapCard(
        habit: Habit,
        accentColorRes: Int,
        cellCount: Int = 35,
        columns: Int = 7,
        cellSizeDp: Int = 10
    ): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_card_rounded)
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dpToPx(8) }
        }

        val nameLabel = TextView(requireContext()).apply {
            text = habit.name
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        card.addView(nameLabel)

        val statsRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(8) }
        }

        val dot = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(8), dpToPx(8)).apply { marginEnd = dpToPx(4) }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot_accent)?.mutate()?.apply {
                setTint(ContextCompat.getColor(requireContext(), accentColorRes))
            }
        }
        statsRow.addView(dot)

        val target = habit.targetValue
        val recorded = habit.todayValueRecorded ?: 0.0
        val todayPercent = when {
            target != null && target > 0 -> ((recorded / target) * 100).toInt().coerceIn(0, 100)
            habit.isCompletedToday -> 100
            else -> 0
        }

        statsRow.addView(TextView(requireContext()).apply {
            text = "$todayPercent%"
            textSize = 11f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.stats_text_muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dpToPx(10) }
        })

        statsRow.addView(TextView(requireContext()).apply {
            text = "${habit.currentStreak} days"
            textSize = 11f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.stats_text_muted))
        })

        card.addView(statsRow)

        val grid = GridLayout(requireContext()).apply {
            columnCount = columns
        }
        repeat(cellCount) { index ->
            val isTodayCell = index == cellCount - 1
            grid.addView(View(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dpToPx(cellSizeDp)
                    height = dpToPx(cellSizeDp)
                    columnSpec = GridLayout.spec(index % columns)
                    rowSpec = GridLayout.spec(index / columns)
                    setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1))
                }
                if (isTodayCell && habit.isCompletedToday) {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), accentColorRes))
                } else {
                    setBackgroundResource(R.drawable.bg_heatmap_cell_empty)
                }
            })
        }
        card.addView(grid)

        return card
    }

    private fun renderYearHabitCards() {
        val container = binding.viewStatsYear.habitHeatmapCardsYear
        container.removeAllViews()

        habits.forEachIndexed { index, habit ->
            container.addView(
                makeHabitHeatmapCard(
                    habit = habit,
                    accentColorRes = habitCardColors[index % habitCardColors.size],
                    cellCount = 365,
                    columns = 52,
                    cellSizeDp = 10
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}