package com.example.bloodbank

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bloodbank.Model.User
import com.example.bloodbank.Util.NotificationHelper
import com.example.bloodbank.databinding.ActivityEmergencyRequestBinding // Assuming binding is used or will be
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyRequestActivity : AppCompatActivity() {

    // Using view binding for cleaner view access (assuming ActivityEmergencyRequestBinding exists from layout name)
    private lateinit var binding: ActivityEmergencyRequestBinding

    private val viewModel: EmergencyRequestViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // For requesting multiple permissions (Location and SMS)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false

        if (fineLocationGranted) {
            // Permission for location granted, try to get last location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Pass location to ViewModel for submission
                    submitEmergencyRequest(location)
                }
                .addOnFailureListener {
                    // Handle cases where location is not available even with permission
                    Toast.makeText(this, "Could not get current location. Submitting request without it.", Toast.LENGTH_LONG).show()
                    submitEmergencyRequest(null)
                }
        } else {
            // Location permission denied, submit without location
            Toast.makeText(this, "Location permission denied, request will be submitted without location data.", Toast.LENGTH_LONG).show()
            submitEmergencyRequest(null)
        }

        if (!smsGranted) {
            Toast.makeText(this, "SMS permission denied. Emergency SMS alerts may not function.", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize view binding
        binding = ActivityEmergencyRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Create Emergency Request"
        }

        // Initialize notification channel (still an Activity responsibility to create)
        NotificationHelper.createNotificationChannel(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup blood group dropdown
        val bloodGroupAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.BLOOD_GROUPS
        )
        binding.bloodGroupInput.setAdapter(bloodGroupAdapter)
        binding.bloodGroupInput.keyListener = null // Disable keyboard input
        binding.bloodGroupInput.setOnClickListener { binding.bloodGroupInput.showDropDown() }

        // Setup priority level dropdown
        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.PRIORITY_LEVELS
        )
        binding.priorityLevelInput.setAdapter(priorityAdapter)
        binding.priorityLevelInput.keyListener = null // Disable keyboard input
        binding.priorityLevelInput.setOnClickListener { binding.priorityLevelInput.showDropDown() }

        binding.submitRequestButton.setOnClickListener {
            // Trigger permission check before submitting
            checkPermissionsAndSubmitRequest()
        }

        observeViewModel()
    }

    private fun checkPermissionsAndSubmitRequest() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions already granted, proceed with getting location and submitting
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    submitEmergencyRequest(location)
                }
                .addOnFailureListener {
                    // Handle cases where location is not available even with permission
                    Toast.makeText(this, "Could not get current location. Submitting request without it.", Toast.LENGTH_LONG).show()
                    submitEmergencyRequest(null)
                }
        }
    }

    private fun submitEmergencyRequest(location: Location?) {
        viewModel.createEmergencyRequest(
            hospitalName = binding.hospitalNameInput.text.toString().trim(),
            hospitalAddress = binding.hospitalAddressInput.text.toString().trim(),
            hospitalContact = binding.hospitalContactInput.text.toString().trim(),
            patientName = binding.patientNameInput.text.toString().trim(),
            bloodGroup = binding.bloodGroupInput.text.toString().trim(),
            priorityLevel = binding.priorityLevelInput.text.toString().trim(),
            unitsNeeded = binding.unitsNeededInput.text.toString().trim().toIntOrNull() ?: 0,
            emergencyDetails = binding.emergencyDetailsInput.text.toString().trim(),
            latitude = location?.latitude,
            longitude = location?.longitude
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.submitRequestButton.isEnabled = !isLoading // Disable button while loading
                    }
                }
                // Observe error messages
                launch {
                    viewModel.errorMessage.collectLatest { message ->
                        message?.let {
                            Toast.makeText(this@EmergencyRequestActivity, it, Toast.LENGTH_LONG).show()
                            // Clear error message after showing
                            viewModel.clearErrorMessage()
                        }
                    }
                }
                // Observe request creation success
                launch {
                    viewModel.requestCreationSuccess.collectLatest { success ->
                        if (success) {
                            Toast.makeText(this@EmergencyRequestActivity, "Emergency request submitted successfully!", Toast.LENGTH_SHORT).show()
                            finish() // Close activity on success
                        }
                    }
                }
                // Observe SMS event to trigger SMS sending (Activity responsibility)
                launch {
                    viewModel.smsEvent.collectLatest { event ->
                        event.getContentIfNotHandled()?.let { (phoneNumber, hospitalName, bloodGroup) ->
                            sendEmergencySMS(phoneNumber, hospitalName, bloodGroup)
                        }
                    }
                }
                // Observe Notification event to trigger notification (Activity responsibility)
                launch {
                    viewModel.notificationEvent.collectLatest { event ->
                        event.getContentIfNotHandled()?.let { (donor, requestId, bloodGroup) ->
                            sendNotificationToDonor(donor, requestId, bloodGroup)
                        }
                    }
                }
            }
        }
    }

    // Function to handle SMS sending - called from ViewModel event
    private fun sendEmergencySMS(phoneNumber: String, hospitalName: String, bloodGroup: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault() // Note: SmsManager.getDefault() is deprecated in API 31+, consider SmsManager.getSystemSmsManager(Context)
                val message = "EMERGENCY: Blood donation needed at $hospitalName for blood group $bloodGroup. Please respond ASAP."
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "Emergency SMS sent to contact.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Should ideally not happen if permission flow is correct, but as a fallback
            Toast.makeText(this, "SMS permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle sending notification - called from ViewModel event
    private fun sendNotificationToDonor(donor: User, requestId: String, bloodGroup: String) {
        val title = "Emergency Blood Request"
        val message = "Urgent need for $bloodGroup blood. Can you help?"

        // Assuming NotificationHelper handles the actual display of local notification
        NotificationHelper.sendEmergencyNotification(this, title, message, requestId, 3)
        // Storing notification in Firebase is now handled by the ViewModel via the Repository
    }

    // Handle up navigation (back button in toolbar)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}