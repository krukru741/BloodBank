package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bloodbank.Adapter.AppointmentsAdapter
import com.example.bloodbank.Model.DonationAppointment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyAppointmentsActivity : AppCompatActivity() {
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar 
    private lateinit var emptyView: TextView

    private val viewModel: MyAppointmentsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        observeViewModel()

        // Trigger initial load
        viewModel.loadAppointments()
    }

    private fun initializeViews() {
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "My Appointments"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        // Initialize adapter with an empty list.
        adapter = AppointmentsAdapter(mutableListOf()) 
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentsRecyclerView.adapter = adapter
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAppointments()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe appointments
                launch {
                    viewModel.appointments.collectLatest { appointments ->
                        adapter.updateAppointments(appointments) 
                        emptyView.visibility = if (appointments.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        swipeRefreshLayout.isRefreshing = isLoading
                    }
                }

                // Observe error events
                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        Toast.makeText(this@MyAppointmentsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() 
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
