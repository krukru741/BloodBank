package com.example.bloodbank

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bloodbank.Model.DonorHealth
import com.example.bloodbank.repository.Result
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DonorHealthActivity : AppCompatActivity() {

    private lateinit var lastDonationDateText: TextView
    private lateinit var totalDonationsText: TextView
    private lateinit var eligibilityStatusText: TextView
    private lateinit var daysUntilEligibleText: TextView
    private lateinit var lastUpdatedText: TextView
    private lateinit var healthStatusText: TextView
    private lateinit var deferralReasonText: TextView

    private lateinit var hemoglobinInput: TextInputEditText
    private lateinit var systolicInput: TextInputEditText
    private lateinit var diastolicInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private lateinit var temperatureInput: TextInputEditText
    private lateinit var pulseRateInput: TextInputEditText

    private lateinit var feelingWellCheckbox: MaterialCheckBox
    private lateinit var medicationCheckbox: MaterialCheckBox
    private lateinit var travelCheckbox: MaterialCheckBox
    private lateinit var surgeryCheckbox: MaterialCheckBox
    private lateinit var pregnancyCheckbox: MaterialCheckBox

    private lateinit var updateHealthMetricsButton: MaterialButton
    private lateinit var progressBar: View

    private val viewModel: DonorHealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_health)

        setupToolbar()
        initializeViews()
        observeViewModel()

        updateHealthMetricsButton.setOnClickListener {
            updateHealthMetrics()
        }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Health Tracking"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initializeViews() {
        lastDonationDateText = findViewById(R.id.lastDonationDateText)
        totalDonationsText = findViewById(R.id.totalDonationsText)
        eligibilityStatusText = findViewById(R.id.eligibilityStatusText)
        daysUntilEligibleText = findViewById(R.id.daysUntilEligibleText)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)
        healthStatusText = findViewById(R.id.healthStatusText)
        deferralReasonText = findViewById(R.id.deferralReasonText)

        hemoglobinInput = findViewById(R.id.hemoglobinInput)
        systolicInput = findViewById(R.id.systolicInput)
        diastolicInput = findViewById(R.id.diastolicInput)
        weightInput = findViewById(R.id.weightInput)
        temperatureInput = findViewById(R.id.temperatureInput)
        pulseRateInput = findViewById(R.id.pulseRateInput)

        feelingWellCheckbox = findViewById(R.id.feelingWellCheckbox)
        medicationCheckbox = findViewById(R.id.medicationCheckbox)
        travelCheckbox = findViewById(R.id.travelCheckbox)
        surgeryCheckbox = findViewById(R.id.surgeryCheckbox)
        pregnancyCheckbox = findViewById(R.id.pregnancyCheckbox)

        updateHealthMetricsButton = findViewById(R.id.updateHealthMetricsButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.donorHealth.collectLatest { donorHealth ->
                        donorHealth?.let {
                            updateUI(it)
                        } ?: run {
                            // Optionally clear UI or show default state if health data is null
                            clearUI()
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        updateHealthMetricsButton.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        Toast.makeText(this@DonorHealthActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                launch {
                    viewModel.updateResult.collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                Toast.makeText(this@DonorHealthActivity, "Health metrics updated successfully", Toast.LENGTH_SHORT).show()
                                // No explicit UI update here, as donorHealth flow will trigger updateUI
                            }
                            is Result.Error -> {
                                Toast.makeText(this@DonorHealthActivity, "Failed to update health metrics: ${result.exception.message}", Toast.LENGTH_LONG).show()
                            }
                            else -> {} // Handle other states if any
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(health: DonorHealth) {
        lastDonationDateText.text = if (health.lastDonationDate != 0L) formatDate(health.lastDonationDate) else "N/A"
        totalDonationsText.text = health.totalDonations.toString()

        eligibilityStatusText.text = health.lastHealthStatus // ViewModel now determines this
        if (health.lastHealthStatus == "Eligible") {
            daysUntilEligibleText.visibility = View.GONE
        } else {
            val days = viewModel.calculateDaysUntilEligible(health)
            if (days > 0) {
                daysUntilEligibleText.text = days.toString()
                daysUntilEligibleText.visibility = View.VISIBLE
            } else {
                daysUntilEligibleText.visibility = View.GONE
            }
        }

        lastUpdatedText.text = if (health.lastUpdated != 0L) "Last Updated: ${formatDate(health.lastUpdated)}" else "Last Updated: N/A"
        healthStatusText.text = "Current Status: ${health.lastHealthStatus}"

        if (health.deferralReason?.isNotBlank() == true) {
            deferralReasonText.text = "Deferral Reason: ${health.deferralReason}"
            deferralReasonText.visibility = View.VISIBLE
        } else {
            deferralReasonText.visibility = View.GONE
        }

        // Pre-fill input fields
        hemoglobinInput.setText(health.hemoglobinLevel?.toString() ?: "")
        systolicInput.setText(health.bloodPressureSystolic?.toString() ?: "")
        diastolicInput.setText(health.bloodPressureDiastolic?.toString() ?: "")
        weightInput.setText(health.weight?.toString() ?: "")
        temperatureInput.setText(health.temperature?.toString() ?: "")
        pulseRateInput.setText(health.pulseRate?.toString() ?: "")

        feelingWellCheckbox.isChecked = health.feelingWell ?: true
        medicationCheckbox.isChecked = health.takenMedication ?: false
        travelCheckbox.isChecked = health.traveled ?: false
        surgeryCheckbox.isChecked = health.hadSurgery ?: false
        pregnancyCheckbox.isChecked = health.pregnant ?: false
    }

    private fun clearUI() {
        lastDonationDateText.text = "N/A"
        totalDonationsText.text = "0"
        eligibilityStatusText.text = "N/A"
        daysUntilEligibleText.visibility = View.GONE
        lastUpdatedText.text = "Last Updated: N/A"
        healthStatusText.text = "Current Status: N/A"
        deferralReasonText.visibility = View.GONE

        hemoglobinInput.setText("")
        systolicInput.setText("")
        diastolicInput.setText("")
        weightInput.setText("")
        temperatureInput.setText("")
        pulseRateInput.setText("")

        feelingWellCheckbox.isChecked = false
        medicationCheckbox.isChecked = false
        travelCheckbox.isChecked = false
        surgeryCheckbox.isChecked = false
        pregnancyCheckbox.isChecked = false
    }

    private fun updateHealthMetrics() {
        val hemoglobinStr = hemoglobinInput.text.toString()
        val systolicStr = systolicInput.text.toString()
        val diastolicStr = diastolicInput.text.toString()
        val weightStr = weightInput.text.toString()
        val temperatureStr = temperatureInput.text.toString()
        val pulseRateStr = pulseRateInput.text.toString()

        viewModel.updateHealthMetrics(
            hemoglobinStr = hemoglobinStr,
            systolicStr = systolicStr,
            diastolicStr = diastolicStr,
            weightStr = weightStr,
            temperatureStr = temperatureStr,
            pulseRateStr = pulseRateStr,
            feelingWell = feelingWellCheckbox.isChecked,
            takenMedication = medicationCheckbox.isChecked,
            traveled = travelCheckbox.isChecked,
            hadSurgery = surgeryCheckbox.isChecked,
            pregnant = pregnancyCheckbox.isChecked
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Modern way to handle back press
        return true
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}