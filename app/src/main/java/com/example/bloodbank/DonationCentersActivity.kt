package com.example.bloodbank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Model.DonationCenter
import com.example.bloodbank.adapter.DonationCentersAdapter
import com.example.bloodbank.repository.Result
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DonationCentersActivity : AppCompatActivity() {

    private lateinit var centersRecyclerView: RecyclerView
    private lateinit var adapter: DonationCentersAdapter
    private lateinit var centersList: MutableList<DonationCenter>
    private lateinit var progressBar: View

    private val viewModel: DonationCentersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_centers)

        // Setup toolbar
        setupToolbar()

        // Initialize views
        centersRecyclerView = findViewById(R.id.centersRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        val fab: FloatingActionButton = findViewById(R.id.fabAddDonationCenter)

        // Setup RecyclerView
        centersList = ArrayList()
        adapter = DonationCentersAdapter(centersList) // Assuming adapter uses Kotlin DonationCenter
        centersRecyclerView.layoutManager = LinearLayoutManager(this)
        centersRecyclerView.adapter = adapter

        // Setup FAB click listener
        fab.setOnClickListener { showAddDonationCenterDialog() }

        // Observe ViewModel data
        observeViewModel()

        // Load donation centers (already triggered by ViewModel init)
        // viewModel.loadDonationCenters() // Redundant as it's called in ViewModel init block
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Donation Centers"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe centers list
                launch {
                    viewModel.centers.collectLatest { centers ->
                        centersList.clear()
                        centersList.addAll(centers)
                        adapter.notifyDataSetChanged()
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                // Observe error messages
                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(this@DonationCentersActivity, "Error: $it", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showAddDonationCenterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_donation_center, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.editTextName)
        val addressInput = dialogView.findViewById<EditText>(R.id.editTextAddress)
        val phoneInput = dialogView.findViewById<EditText>(R.id.editTextPhone)
        val emailInput = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val citySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCity)

        // Setup city spinner with hardcoded cities
        val cities = arrayOf("Manila", "Quezon City", "Makati", "Pasig", "Taguig", "Cebu", "Davao")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter

        AlertDialog.Builder(this)
            .setTitle("Add Donation Center")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, which ->
                val name = nameInput.text.toString().trim()
                val address = addressInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val city = citySpinner.selectedItem.toString()

                if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                viewModel.addDonationCenter(
                    name = name,
                    address = address,
                    phone = phone,
                    email = email,
                    latitude = 0.0,
                    longitude = 0.0,
                    operatingHours = "8:00 AM - 5:00 PM",
                    maxDailyAppointments = 50,
                    isActive = true
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Modern way to handle back press
        return true
    }

    // No need to remove listeners manually in onDestroy as Flows are lifecycle-aware
    // and managed by viewModelScope and repeatOnLifecycle.
}