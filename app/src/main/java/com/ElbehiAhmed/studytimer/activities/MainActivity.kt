package com.ElbehiAhmed.studytimer.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ElbehiAhmed.studytimer.R
import com.ElbehiAhmed.studytimer.database.AppDatabase
import com.ElbehiAhmed.studytimer.databinding.ActivityMainBinding
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.utils.TimeUtils
import com.ElbehiAhmed.studytimer.viewmodel.TimerState
import com.ElbehiAhmed.studytimer.viewmodel.TimerViewModel
import com.ElbehiAhmed.studytimer.viewmodel.TimerViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(
            SessionRepository(
                AppDatabase.getDatabase(applicationContext).studySessionDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupButtonClicks()
        observeViewModel()
    }

    private fun setupButtonClicks() {
        binding.btnStart.setOnClickListener {
            viewModel.startTimer()
        }

        binding.btnPauseResume.setOnClickListener {
            val state = viewModel.timerState.value
            if (state == TimerState.Running) {
                viewModel.pauseTimer()
            } else if (state == TimerState.Paused) {
                viewModel.resumeTimer()
            }
        }

        binding.btnStop.setOnClickListener {
            val elapsed = viewModel.elapsedSeconds.value
            viewModel.stopTimer()
            if (elapsed > 0) {
                Toast.makeText(this, "Session saved! (${TimeUtils.formatDuration(elapsed)})", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fabHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.timerState.collect { state ->
                        updateButtonsForState(state)
                    }
                }
                launch {
                    viewModel.elapsedSeconds.collect { seconds ->
                        binding.tvElapsedTime.text = TimeUtils.formatDuration(seconds)
                    }
                }
            }
        }
    }

    private fun updateButtonsForState(state: TimerState) {
        when (state) {
            TimerState.Idle -> {
                binding.btnStart.isEnabled = true
                binding.btnPauseResume.isEnabled = false
                binding.btnStop.isEnabled = false
                binding.btnPauseResume.text = "Pause"
            }
            TimerState.Running -> {
                binding.btnStart.isEnabled = false
                binding.btnPauseResume.isEnabled = true
                binding.btnStop.isEnabled = true
                binding.btnPauseResume.text = "Pause"
            }
            TimerState.Paused -> {
                binding.btnStart.isEnabled = false
                binding.btnPauseResume.isEnabled = true
                binding.btnStop.isEnabled = true
                binding.btnPauseResume.text = "Resume"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_statistics -> {
                startActivity(Intent(this, StatisticsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}