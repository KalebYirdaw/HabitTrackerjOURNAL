package com.example.habittracker.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.habittracker.R
import com.example.habittracker.api.TokenManager
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.HomeFragment
import com.example.habittracker.ui.JournalFragment
import com.example.habittracker.ui.SettingsFragment
import com.example.habittracker.ui.StatsFragment
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAB = "extra_tab"
        const val TAB_HABITS = "habits"
        const val TAB_STATS = "stats"
        const val TAB_JOURNAL = "journal"
        const val TAB_SETTINGS = "settings"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHabits: LinearLayout
    private lateinit var navStats: LinearLayout
    private lateinit var navAdd: LinearLayout
    private lateinit var navJournal: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // checks if JWT exists in TokenManager
        val tokenManager = TokenManager(this)
        // it redirects the user to LoginActivity
        if (tokenManager.getToken().isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        navHabits = findViewById(R.id.nav_habits)
        navStats = findViewById(R.id.nav_stats)
        navAdd = findViewById(R.id.nav_add)
        navJournal = findViewById(R.id.nav_journal)
        navSettings = findViewById(R.id.nav_settings)

        // reads EXTRA_TAB from incoming Intents so external activities can direct the user back to a specific tab upon finishing.
        if (savedInstanceState == null) {
            when (intent.getStringExtra(EXTRA_TAB)) {
                TAB_STATS -> {
                    loadFragment(StatsFragment())
                    setSelectedTab(navStats)
                }
                TAB_JOURNAL -> {
                    loadFragment(JournalFragment())
                    setSelectedTab(navJournal)
                }
                TAB_SETTINGS -> {
                    loadFragment(SettingsFragment())
                    setSelectedTab(navSettings)
                }
                else -> {
                    loadFragment(HomeFragment())
                    setSelectedTab(navHabits)
                }
            }
        }

        // habit
        navHabits.setOnClickListener {
            loadFragment(HomeFragment())
            setSelectedTab(navHabits)
        }

        // stat
        navStats.setOnClickListener {
            loadFragment(StatsFragment())
            setSelectedTab(navStats)
        }

        // journal
        navJournal.setOnClickListener {
            loadFragment(JournalFragment())
            setSelectedTab(navJournal)
        }

        // settings
        navSettings.setOnClickListener {
            loadFragment(SettingsFragment())
            setSelectedTab(navSettings)
        }

        // add habit btn
        navAdd.setOnClickListener {
            showAddHabitFlow()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    // opens passed fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // highlights selected tab
    private fun setSelectedTab(selected: LinearLayout) {
        val tabs = listOf(
            Triple(navHabits, R.drawable.ic_habits_bold, R.drawable.ic_habits_home),
            Triple(navStats, R.drawable.ic_graph_bold, R.drawable.ic_graph_bar_light),
            Triple(navJournal, R.drawable.ic_journal_bold, R.drawable.ic_journal_light),
            Triple(navSettings, R.drawable.ic_settings_bold, R.drawable.ic_setting_light)
        )

        for ((tab, boldIcon, lightIcon) in tabs) {
            val icon = tab.getChildAt(0) as? ImageView
            val label = tab.getChildAt(1) as? TextView
            val isSelected = tab === selected

            icon?.setImageResource(if (isSelected) boldIcon else lightIcon)
            val colorRes = if (isSelected) R.color.nav_icon_selected else R.color.nav_icon_default
            label?.setTextColor(ContextCompat.getColor(this, colorRes))
        }
    }

    // open CreateHabitActivity
    private fun showAddHabitFlow() {
        startActivity(Intent(this, CreateHabitActivity::class.java))
    }

    fun navigateToHomeWithDate(date: Calendar) {
        val homeFragment = HomeFragment.newInstance(date)
        loadFragment(homeFragment)
        setSelectedTab(navHabits)
    }
}