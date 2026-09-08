package com.ElbehiAhmed.studytimer.activities
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.ElbehiAhmed.studytimer.utils.RelayMqttClient
import com.ElbehiAhmed.studytimer.utils.TimeUtils
import com.ElbehiAhmed.studytimer.viewmodel.TimerState
import com.ElbehiAhmed.studytimer.viewmodel.TimerViewModel
import com.ElbehiAhmed.studytimer.viewmodel.TimerViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

// MQTT topics used by this screen.
private const val TOPIC_START = "home/start"
private const val TOPIC_BRIGHTNESS = "home/brightness"
private const val TOPIC_TEMPERATURE = "home/temp"
private const val TOPIC_HUMIDITY = "home/humidity"
private const val TOPIC_STATE = "home/state"
private const val TOPIC_MODE = "home/mode"

// How long we wait for an "on" heartbeat on home/state before we assume the device is offline.
private const val STATE_WATCHDOG_TIMEOUT_MS = 10_000L

// The text shown on the dashboard when we haven't heard "on" recently (or at all).
private const val STATE_OFFLINE_TEXT = "offline"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Manual (default): user drives the slider. Automatic: slider is frozen/remote-driven.
    private var isAutomaticMode = false

    // Prevents programmatic slider updates from re-triggering publish/timer logic.
    private var isUpdatingSliderProgrammatically = false
    private val brightnessHandler = Handler(Looper.getMainLooper())
    private var latestTemperature: Double? = null
    private var latestHumidity: Double? = null
    private var latestBrightness: Double? = null
    private val sessionTemperatures = mutableListOf<Double>()
    private val sessionBrightnessValues = mutableListOf<Double>()

    // Watchdog for the home/state dashboard value: if "on" isn't seen again within
    // STATE_WATCHDOG_TIMEOUT_MS, we assume the device went offline and show "offline".
    private val stateWatchdogHandler = Handler(Looper.getMainLooper())
    private val stateWatchdogRunnable = Runnable {
        binding.tvStateValue.text = STATE_OFFLINE_TEXT   // ← just a local UI update, nothing published
    }

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

        // Default state: assume offline until we actually hear an "on" heartbeat from the ESP32.
        binding.tvStateValue.text = STATE_OFFLINE_TEXT

        setupModeToggle()
        setupSlider()
        setupOtherClicks()
        observeViewModel()
        subscribeToDashboardTopics()

        // Announce the app is up.
        publishToTopic(TOPIC_START, "on")
    }

    override fun onDestroy() {
        brightnessHandler.removeCallbacksAndMessages(null)
        stateWatchdogHandler.removeCallbacksAndMessages(null)

        // onDestroy also fires on configuration changes (e.g. rotation), not just when the
        // app is actually being closed. Only publish "off" when the activity is really
        // finishing (back press, task removed, finish() called), otherwise a rotation
        // would incorrectly tell the device to turn off.
        if (isFinishing) {
            // onDestroy cancels lifecycleScope; use runBlocking to ensure "off" is sent before exit.
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        // Increase timeout slightly to allow for connection if needed
                        withTimeoutOrNull(3000L) {
                            RelayMqttClient.publish(TOPIC_START, "off")
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "MQTT onDestroy publish failed", e)
                    } finally {
                        RelayMqttClient.disconnect()
                    }
                }
            }
        }
        super.onDestroy()
    }

    private fun setupOtherClicks() {
        binding.fabHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnGithub.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/elbehi-ahmed/IoT-Smart-Lighting-System"))
            )
        }
    }

    // Manual/Automatic toggle. Manual is selected by default.
    private fun setupModeToggle() {
        binding.toggleMode.check(R.id.btnManual)
        applyModeToSlider()

        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            isAutomaticMode = checkedId == R.id.btnAutomatic
            applyModeToSlider()
            publishToTopic(TOPIC_MODE, if (isAutomaticMode) "automatic" else "manual")
        }
    }

    private fun applyModeToSlider() {
        binding.sliderBrightness.isEnabled = !isAutomaticMode
        binding.tvModeHint.text = if (isAutomaticMode) {
            getString(R.string.mode_automatic_hint)
        } else {
            getString(R.string.mode_manual_hint)
        }
    }

    // Brightness slider replaces Start/Stop; drives publishing + the timer.
    private fun setupSlider() {
        binding.sliderBrightness.valueFrom = 0f
        binding.sliderBrightness.valueTo = 100f
        binding.sliderBrightness.stepSize = 1f

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            binding.tvSliderValue.text = value.toInt().toString()
            latestBrightness = value.toDouble()

            if (!fromUser || isUpdatingSliderProgrammatically) return@addOnChangeListener

            brightnessHandler.removeCallbacksAndMessages(null)
            brightnessHandler.postDelayed({
                publishToTopic(TOPIC_BRIGHTNESS, value.toInt().toString())
            }, 25L)

            val isTimerRunning = viewModel.timerState.value == TimerState.Running
            when {
                value > 0f && !isTimerRunning -> {
                    sessionTemperatures.clear()
                    sessionBrightnessValues.clear()
                    latestTemperature?.let(sessionTemperatures::add)
                    sessionBrightnessValues.add(value.toDouble())
                    viewModel.startTimer()
                }
                value > 0f && isTimerRunning -> {
                    sessionBrightnessValues.add(value.toDouble())
                }
                value == 0f && isTimerRunning -> {
                    val elapsed = viewModel.elapsedSeconds.value
                    val sessionTemperature = sessionTemperatures.average().takeIf {
                        sessionTemperatures.isNotEmpty()
                    }
                    val sessionBrightness = sessionBrightnessValues.average().takeIf {
                        sessionBrightnessValues.isNotEmpty()
                    }
                    viewModel.stopTimer(sessionTemperature, latestHumidity, sessionBrightness)
                    sessionTemperatures.clear()
                    sessionBrightnessValues.clear()
                    if (elapsed > 0) {
                        Toast.makeText(
                            this,
                            "Session saved! (${TimeUtils.formatDuration(elapsed)})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.elapsedSeconds.collect { seconds ->
                        binding.tvElapsedTime.text = TimeUtils.formatDuration(seconds)
                    }
                }
            }
        }
    }

    // Dashboard: subscribe to sensor/state topics and fill in the values shown.
    private fun subscribeToDashboardTopics() {
        subscribeTopic(TOPIC_TEMPERATURE) { payload ->
            binding.tvTemperatureValue.text = "$payload°C"
            latestTemperature = payload.toDoubleOrNull()
            if (viewModel.timerState.value == TimerState.Running) {
                latestTemperature?.let(sessionTemperatures::add)
            }
        }
        subscribeTopic(TOPIC_HUMIDITY) { payload ->
            binding.tvHumidityValue.text = "$payload%"
            latestHumidity = payload.toDoubleOrNull()
        }
        subscribeTopic(TOPIC_STATE) { payload ->
            handleStatePayload(payload)
        }
    }

    // If "on" isn't republished on home/state within STATE_WATCHDOG_TIMEOUT_MS, we show
    // "offline" on the dashboard. Every time "on" arrives we (re)arm a fresh countdown;
    // if it fires without a new "on" showing up, we fall back to offline.
    private fun handleStatePayload(payload: String) {
        stateWatchdogHandler.removeCallbacks(stateWatchdogRunnable) // cancel any pending timeout

        binding.tvStateValue.text = payload   // show whatever the ESP32 sent on home/state

        if (payload.equals("online", ignoreCase = true)) {
            // (re)arm a fresh countdown every time "online" arrives
            stateWatchdogHandler.postDelayed(stateWatchdogRunnable, STATE_WATCHDOG_TIMEOUT_MS)
        }

        if (payload.equals("connecting", ignoreCase = true)) {
            publishToTopic(TOPIC_START, "on")
        }
    }
    private fun subscribeTopic(topic: String, onMessage: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RelayMqttClient.subscribe(topic) { payload ->
                        runOnUiThread { onMessage(payload) }
                    }
                }
            } catch (exception: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "MQTT subscribe failed ($topic): ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun publishToTopic(topic: String, message: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RelayMqttClient.publish(topic, message)
                }
            } catch (exception: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "MQTT publish failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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