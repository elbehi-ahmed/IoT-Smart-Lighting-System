package com.ElbehiAhmed.studytimer.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ElbehiAhmed.studytimer.databinding.ActivityMainBinding
import com.ElbehiAhmed.studytimer.viewmodel.TimerState
import com.ElbehiAhmed.studytimer.viewmodel.TimerViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 'by viewModels()' = Kotlin property delegate.
    // It creates (or reuses, if surviving rotation) the ViewModel instance for us.
    private val viewModel: TimerViewModel by viewModels()

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
            viewModel.stopTimer()
            // Session saving to Room comes in the next milestone
        }
    }

    private fun observeViewModel() {
        // repeatOnLifecycle: only collect while Activity is STARTED (visible),
        // auto-pauses/resumes collection with the lifecycle — avoids wasted work/leaks.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.timerState.collect { state ->
                        updateButtonsForState(state)
                    }
                }
                launch {
                    viewModel.elapsedSeconds.collect { seconds ->
                        binding.tvElapsedTime.text = formatTime(seconds)
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

    private fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}