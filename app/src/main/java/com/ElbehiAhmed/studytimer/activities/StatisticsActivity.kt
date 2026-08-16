package com.ElbehiAhmed.studytimer.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ElbehiAhmed.studytimer.database.AppDatabase
import com.ElbehiAhmed.studytimer.databinding.ActivityStatisticsBinding
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.utils.SessionStats
import com.ElbehiAhmed.studytimer.utils.TimeUtils
import com.ElbehiAhmed.studytimer.viewmodel.StatisticsViewModel
import com.ElbehiAhmed.studytimer.viewmodel.StatisticsViewModelFactory
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    private val viewModel: StatisticsViewModel by viewModels {
        StatisticsViewModelFactory(
            SessionRepository(
                AppDatabase.getDatabase(applicationContext).studySessionDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeStats()
    }

    private fun observeStats() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { stats ->
                    renderStats(stats)
                }
            }
        }
    }

    private fun renderStats(stats: SessionStats) {
        binding.tvTotalTime.text = "Total Time: ${TimeUtils.formatDuration(stats.totalSeconds)}"
        binding.tvSessionCount.text = "Sessions: ${stats.sessionCount}"
        binding.tvAverage.text = "Average Session: ${TimeUtils.formatDuration(stats.averageSeconds)}"
        binding.tvLongest.text = "Longest Session: ${TimeUtils.formatDuration(stats.longestSeconds)}"
        binding.tvShortest.text = "Shortest Session: ${TimeUtils.formatDuration(stats.shortestSeconds)}"

        binding.tvTodayLabel.text = "Today: ${TimeUtils.formatDuration(stats.todaySeconds)}"
        binding.tvWeekLabel.text = "This Week: ${TimeUtils.formatDuration(stats.weekSeconds)}"
        binding.tvMonthLabel.text = "This Month: ${TimeUtils.formatDuration(stats.monthSeconds)}"

        // Progress bars: show today/week/month as a % of total time (guard against divide-by-zero)
        if (stats.totalSeconds > 0) {
            binding.progressToday.progress =
                ((stats.todaySeconds * 100) / stats.totalSeconds).toInt()
            binding.progressWeek.progress =
                ((stats.weekSeconds * 100) / stats.totalSeconds).toInt()
            binding.progressMonth.progress =
                ((stats.monthSeconds * 100) / stats.totalSeconds).toInt()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}