package com.example.habittracker.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.habittracker.R

/* It creates a custom bottom navigation bar for the habit tracker app */
class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // defines the navigation destination
    enum class Tab { HABITS, STATS, JOURNAL, SETTINGS }

    private var onTabSelected: ((Tab) -> Unit)? = null
    private var onAddClicked: (() -> Unit)? = null
    private var currentTab: Tab = Tab.HABITS

    private val iconHabits: ImageView
    private val labelHabits: TextView
    private val iconStats: ImageView
    private val labelStats: TextView
    private val iconJournal: ImageView
    private val labelJournal: TextView
    private val iconSettings: ImageView
    private val labelSettings: TextView

    init {
        orientation = HORIZONTAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.nav_bar_bg))
        elevation = 8 * resources.displayMetrics.density
        val hPad = (4 * resources.displayMetrics.density).toInt()
        setPadding(hPad, 0, hPad, 0)

        inflate(context, R.layout.view_bottom_nav_bar, this)

        // binds icon ImageViews and label TextViews
        iconHabits = findViewById(R.id.icon_habits)
        labelHabits = findViewById(R.id.label_habits)
        iconStats = findViewById(R.id.icon_stats)
        labelStats = findViewById(R.id.label_stats)
        iconJournal = findViewById(R.id.icon_journal)
        labelJournal = findViewById(R.id.label_journal)
        iconSettings = findViewById(R.id.icon_settings)
        labelSettings = findViewById(R.id.label_settings)

        // habit
        findViewById<View>(R.id.nav_habits).setOnClickListener {
            selectTab(Tab.HABITS)
            onTabSelected?.invoke(Tab.HABITS)
        }

        // stat
        findViewById<View>(R.id.nav_stats).setOnClickListener {
            selectTab(Tab.STATS)
            onTabSelected?.invoke(Tab.STATS)
        }

        // journal
        findViewById<View>(R.id.nav_journal).setOnClickListener {
            selectTab(Tab.JOURNAL)
            onTabSelected?.invoke(Tab.JOURNAL)
        }

        // settings
        findViewById<View>(R.id.nav_settings).setOnClickListener {
            selectTab(Tab.SETTINGS)
            onTabSelected?.invoke(Tab.SETTINGS)
        }

        // central add habit button
        findViewById<View>(R.id.nav_add).setOnClickListener {
            onAddClicked?.invoke()
        }

        refreshIcons()
    }

    fun setOnTabSelectedListener(listener: (Tab) -> Unit) {
        onTabSelected = listener
    }

    fun setOnAddClickListener(listener: () -> Unit) {
        onAddClicked = listener
    }

    fun selectTab(tab: Tab) {
        currentTab = tab
        refreshIcons()
    }

    private fun refreshIcons() {
        // toggles between bold (ic_habits_bold) for selected state and outlined/light (ic_habits_home) for unselected state.
        setTab(iconHabits, labelHabits, currentTab == Tab.HABITS, R.drawable.ic_habits_bold, R.drawable.ic_habits_home)
        setTab(iconStats, labelStats, currentTab == Tab.STATS, R.drawable.ic_graph_bold, R.drawable.ic_graph_bar_light)
        setTab(iconJournal, labelJournal, currentTab == Tab.JOURNAL, R.drawable.ic_journal_bold, R.drawable.ic_journal_light)
        setTab(iconSettings, labelSettings, currentTab == Tab.SETTINGS, R.drawable.ic_settings_bold, R.drawable.ic_setting_light)
    }

    private fun setTab(icon: ImageView, label: TextView, selected: Boolean, boldRes: Int, lightRes: Int) {
        // adjusting text weight and color tints automatically.
        icon.setImageResource(if (selected) boldRes else lightRes)
        label.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        label.setTextColor(
            ContextCompat.getColor(
                context,
                if (selected) R.color.nav_icon_selected else R.color.nav_icon_default
            )
        )
    }
}