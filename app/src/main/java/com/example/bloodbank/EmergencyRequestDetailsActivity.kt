package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * EmergencyRequestDetailsActivity - Display details of an emergency request.
 * Uses EmergencyRequestViewModel (already exists from previous migration).
 */
@AndroidEntryPoint
class EmergencyRequestDetailsActivity : AppCompatActivity() {
    
    private val viewModel: EmergencyRequestViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var patientNameText: TextView
    private lateinit var bloodGroupText: TextView
    private lateinit var hospitalText: TextView
    private lateinit var contactText: TextView
    private lateinit var urgencyText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var respondButton: MaterialButton
    
    private var requestId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_request_details)
        
        // Get request ID from intent
        requestId = intent.getStringExtra("requestId") ?: ""
        
        if (requestId.isEmpty()) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initializeViews()
        setupToolbar()
        setupButton()
        observeViewModel()
        
        // Load request details
        viewModel.loadRequestDetails(requestId)
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        patientNameText = findViewById(R.id.patientNameText)
        bloodGroupText = findViewById(R.id.bloodGroupText)
        hospitalText = findViewById(R.id.hospitalText)
        contactText = findViewById(R.id.contactText)
        urgencyText = findViewById(R.id.urgencyText)
        descriptionText = findViewById(R.id.descriptionText)
        respondButton = findViewById(R.id.respondButton)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Emergency Request Details"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupButton() {
        respondButton.setOnClickListener {
            viewModel.respondToRequest(requestId)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedRequest.collect { request ->
                        request?.let {
                            patientNameText.text = it.patientName
                            bloodGroupText.text = "Blood Group: ${it.bloodGroup}"
                            hospitalText.text = "Hospital: ${it.hospital}"
                            contactText.text = "Contact: ${it.contactNumber}"
                            urgencyText.text = "Urgency: ${it.urgencyLevel}"
                            descriptionText.text = it.description
                        }
                    }
                }
                
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@EmergencyRequestDetailsActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
