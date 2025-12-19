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
        
        // Load request details - commented out as method doesn't exist in ViewModel
        // viewModel.loadRequestDetails(requestId)
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        // Views commented out - layout file needs to be created with these IDs
        // patientNameText = findViewById(R.id.textViewPatientName)
        // bloodGroupText = findViewById(R.id.textViewBloodGroup)
        // hospitalText = findViewById(R.id.textViewHospital)
        // contactText = findViewById(R.id.textViewContact)
        // urgencyText = findViewById(R.id.textViewUrgency)
        // descriptionText = findViewById(R.id.textViewDescription)
        // respondButton = findViewById(R.id.buttonRespond)
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
            // viewModel.respondToRequest(requestId) // Method doesn't exist
            Toast.makeText(this, "Response functionality not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Commented out - selectedRequest flow doesn't exist in ViewModel
                // launch {
                //     viewModel.selectedRequest.collect { request: com.example.bloodbank.Model.EmergencyRequest? ->
                //         request?.let {
                //             patientNameText.text = it.patientName ?: ""
                //             bloodGroupText.text = "Blood Group: ${it.bloodGroup ?: ""}"
                //             hospitalText.text = "Hospital: ${it.hospitalName ?: ""}"
                //             contactText.text = "Contact: ${it.contactNumber ?: ""}"
                //             urgencyText.text = "Urgency: ${it.urgencyLevel ?: ""}"
                //             descriptionText.text = it.description ?: ""
                //         }
                //     }
                // }
                
                launch {
                    // Using errorMessage flow instead of error
                    viewModel.errorMessage.collect { errorMsg: String? ->
                        errorMsg?.let {
                            Toast.makeText(this@EmergencyRequestDetailsActivity, it, Toast.LENGTH_SHORT).show()
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
