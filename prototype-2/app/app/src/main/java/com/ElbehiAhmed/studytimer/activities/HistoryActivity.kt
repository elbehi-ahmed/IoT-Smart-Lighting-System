package com.ElbehiAhmed.studytimer.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ElbehiAhmed.studytimer.R
import com.ElbehiAhmed.studytimer.adapters.SessionAdapter
import com.ElbehiAhmed.studytimer.database.AppDatabase
import com.ElbehiAhmed.studytimer.databinding.ActivityHistoryBinding
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.viewmodel.HistoryViewModel
import com.ElbehiAhmed.studytimer.viewmodel.HistoryViewModelFactory
import com.ElbehiAhmed.studytimer.viewmodel.SortMode
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeSessions()
        setupSearch()
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

    private fun setupSearch() {
        binding.etSearchDate.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchDate(s?.toString())
            }
        })
    }

    // Handles the back-arrow tap in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_newest -> {
                viewModel.setSortMode(SortMode.NEWEST)
                true
            }
            R.id.action_sort_oldest -> {
                viewModel.setSortMode(SortMode.OLDEST)
                true
            }
            R.id.action_delete_all -> {
                showDeleteAllConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAllConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Delete All Sessions")
            .setMessage("This will permanently delete all saved sessions. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllSessions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
