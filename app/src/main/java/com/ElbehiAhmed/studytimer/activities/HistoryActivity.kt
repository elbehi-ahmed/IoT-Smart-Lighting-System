package com.ElbehiAhmed.studytimer.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ElbehiAhmed.studytimer.adapters.SessionAdapter
import com.ElbehiAhmed.studytimer.database.AppDatabase
import com.ElbehiAhmed.studytimer.databinding.ActivityHistoryBinding
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.viewmodel.HistoryViewModel
import com.ElbehiAhmed.studytimer.viewmodel.HistoryViewModelFactory
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: SessionAdapter

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            SessionRepository(
                AppDatabase.getDatabase(applicationContext).studySessionDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Show a back arrow in the toolbar so the user can return to MainActivity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeSessions()
    }

    private fun setupRecyclerView() {
        adapter = SessionAdapter(
            onDeleteClick = { session -> viewModel.deleteSession(session) }
        )
        binding.recyclerViewSessions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSessions.adapter = adapter
    }

    private fun observeSessions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessions.collect { sessionList ->
                    adapter.submitList(sessionList)
                }
            }
        }
    }

    // Handles the back-arrow tap in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}