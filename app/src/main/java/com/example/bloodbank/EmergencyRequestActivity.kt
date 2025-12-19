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
import com.example.bloodbank.databinding.ActivityEmergencyRequestBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyRequestBinding

    private val viewModel: EmergencyRequestViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false

        if (fineLocationGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    submitEmergencyRequest(location)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not get current location. Submitting request without it.", Toast.LENGTH_LONG).show()
                    submitEmergencyRequest(null)
                }
        } else {
            Toast.makeText(this, "Location permission denied, request will be submitted without location data.", Toast.LENGTH_LONG).show()
            submitEmergencyRequest(null)
        }

        if (!smsGranted) {
            Toast.makeText(this, "SMS permission denied. Emergency SMS alerts may not function.", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Create Emergency Request"
        }

        // Initialize notification channel
        // NotificationHelper.createNotificationChannel(this) // Commented out - NotificationHelper doesn't exist

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val bloodGroupAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            bloodGroups
        )
        binding.bloodGroupInput.setAdapter(bloodGroupAdapter)
        binding.bloodGroupInput.keyListener = null
        binding.bloodGroupInput.setOnClickListener { binding.bloodGroupInput.showDropDown() }

        val priorityLevels = arrayOf("Critical", "Urgent", "Normal")
        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            priorityLevels
        )
        binding.priorityLevelInput.setAdapter(priorityAdapter)
        binding.priorityLevelInput.keyListener = null
        binding.priorityLevelInput.setOnClickListener { binding.priorityLevelInput.showDropDown() }

        binding.submitRequestButton.setOnClickListener {
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
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    submitEmergencyRequest(location)
                }
                .addOnFailureListener {
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
            unitsNeeded = binding.unitsNeededInput.text.toString().trim(),
            emergencyDetails = binding.emergencyDetailsInput.text.toString().trim(),
            location = location
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading: Boolean ->
                        binding.root.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                            if (isLoading) View.VISIBLE else View.GONE
                        binding.submitRequestButton.isEnabled = !isLoading
                    }
                }

                // Observe error messages
                launch {
                    viewModel.errorMessage.collectLatest { message: String? ->
                        message?.let {
                            Toast.makeText(this@EmergencyRequestActivity, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                // Observe request creation success
                launch {
                    viewModel.requestCreationSuccess.collectLatest { _: Unit ->
                        Toast.makeText(
                            this@EmergencyRequestActivity,
                            "Emergency request submitted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                // Observe SMS events
                launch {
                    viewModel.smsEvent.collectLatest { smsData: com.example.bloodbank.util.Event<Triple<String, String, String>> ->
                        smsData.getContentIfNotHandled()?.let { data ->
                            sendEmergencySMS(data.first, data.second, data.third)
                        }
                    }
                }

                // Observe notification events
                launch {
                    viewModel.notificationEvent.collectLatest { notificationData: com.example.bloodbank.util.Event<Triple<User, String, String>> ->
                        notificationData.getContentIfNotHandled()?.let { data ->
                            sendNotificationToDonor(data.first, data.second, data.third)
                        }
                    }
                }
            }
        }
    }

    private fun sendEmergencySMS(phoneNumber: String, hospitalName: String, bloodGroup: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                val message = "EMERGENCY: Blood donation needed at $hospitalName for blood group $bloodGroup. Please respond ASAP."
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "Emergency SMS sent to contact.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "SMS permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotificationToDonor(donor: User, requestId: String, bloodGroup: String) {
        val title = "Emergency Blood Request"
        val message = "Urgent need for $bloodGroup blood. Can you help?"
        // Assuming NotificationHelper handles the actual display of local notification
        // NotificationHelper.sendEmergencyNotification(this, title, message, requestId, 3) // Commented out - class doesn't exist
        Toast.makeText(this, "$title: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}