package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.DonorHealth
import com.example.bloodbank.repository.DonorHealthRepository
import com.example.bloodbank.repository.Result
import com.example.bloodbank.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DonorHealthViewModel @Inject constructor(
    private val donorHealthRepository: DonorHealthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _donorHealth = MutableStateFlow<DonorHealth?>(null)
    val donorHealth: StateFlow<DonorHealth?> = _donorHealth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _updateResult = MutableSharedFlow<Result<Unit>>()
    val updateResult: SharedFlow<Result<Unit>> = _updateResult.asSharedFlow()

    companion object {
        // Health Metric Constants (moved from Activity)
        const val MIN_HEMOGLOBIN = 12.5 // g/dL
        const val MIN_WEIGHT = 50.0 // kg
        const val MIN_SYSTOLIC = 90
        const val MAX_SYSTOLIC = 180
        const val MIN_DIASTOLIC = 60
        const val MAX_DIASTOLIC = 100
        const val MIN_TEMPERATURE = 36.0 // °C
        const val MAX_TEMPERATURE = 37.5 // °C
        const val MIN_PULSE_RATE = 50 // bpm
        const val MAX_PULSE_RATE = 100 // bpm
        val DONATION_INTERVAL = TimeUnit.DAYS.toMillis(56) // 56 days between donations
    }

    init {
        loadDonorHealth()
    }

    fun loadDonorHealth() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserId = userRepository.getCurrentUserUid()
            if (currentUserId == null) {
                _error.emit("User not logged in.")
                _isLoading.value = false
                return@launch
            }

            donorHealthRepository.getDonorHealth(currentUserId).collect { health ->
                if (health == null) {
                    // No health record found, create a new one
                    val newHealth = DonorHealth(
                        donorId = currentUserId,
                        lastHealthStatus = "ELIGIBLE",
                        lastUpdated = System.currentTimeMillis()
                    )
                    donorHealthRepository.createDonorHealth(currentUserId, newHealth).collect { result ->
                        _isLoading.value = false
                        when (result) {
                            is Result.Success -> _donorHealth.value = newHealth
                            is Result.Error -> _error.emit("Failed to create initial health record: ${result.exception.message}")
                        }
                    }
                } else {
                    _donorHealth.value = health
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateHealthMetrics(
        hemoglobinStr: String,
        systolicStr: String,
        diastolicStr: String,
        weightStr: String,
        temperatureStr: String,
        pulseRateStr: String,
        feelingWell: Boolean,
        takenMedication: Boolean,
        traveled: Boolean,
        hadSurgery: Boolean,
        pregnant: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserId = userRepository.getCurrentUserUid()
            if (currentUserId == null) {
                _error.emit("User not logged in.")
                _isLoading.value = false
                return@launch
            }

            // Input Validation
            if (hemoglobinStr.isEmpty() || systolicStr.isEmpty() ||
                diastolicStr.isEmpty() || weightStr.isEmpty() ||
                temperatureStr.isEmpty() || pulseRateStr.isEmpty()
            ) {
                _error.emit("Please fill in all health metrics fields.")
                _isLoading.value = false
                return@launch
            }

            try {
                val hemoglobin = hemoglobinStr.toDouble()
                val systolic = systolicStr.toInt()
                val diastolic = diastolicStr.toInt()
                val weight = weightStr.toDouble()
                val temperature = temperatureStr.toDouble()
                val pulseRate = pulseRateStr.toInt()

                val currentDonorHealth = _donorHealth.value ?: DonorHealth(donorId = currentUserId)

                // Calculate eligibility and deferral reason
                val deferralReason = StringBuilder()
                var isEligible = true

                if (hemoglobin < MIN_HEMOGLOBIN) {
                    deferralReason.append("Low hemoglobin level. ")
                    isEligible = false
                }
                if (weight < MIN_WEIGHT) {
                    deferralReason.append("Below minimum weight requirement. ")
                    isEligible = false
                }
                if (systolic < MIN_SYSTOLIC || systolic > MAX_SYSTOLIC) {
                    deferralReason.append("Systolic blood pressure out of range ($MIN_SYSTOLIC-$MAX_SYSTOLIC). ")
                    isEligible = false
                }
                if (diastolic < MIN_DIASTOLIC || diastolic > MAX_DIASTOLIC) {
                    deferralReason.append("Diastolic blood pressure out of range ($MIN_DIASTOLIC-$MAX_DIASTOLIC). ")
                    isEligible = false
                }
                if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
                    deferralReason.append("Temperature out of normal range ($MIN_TEMPERATURE-$MAX_TEMPERATURE°C). ")
                    isEligible = false
                }
                if (pulseRate < MIN_PULSE_RATE || pulseRate > MAX_PULSE_RATE) {
                    deferralReason.append("Pulse rate out of normal range ($MIN_PULSE_RATE-$MAX_PULSE_RATE bpm). ")
                    isEligible = false
                }

                if (!feelingWell) {
                    deferralReason.append("Not feeling well. ")
                    isEligible = false
                }
                if (takenMedication) {
                    deferralReason.append("Medication taken. ")
                    isEligible = false
                }
                if (traveled) {
                    deferralReason.append("Recent international travel. ")
                    isEligible = false
                }
                if (hadSurgery) {
                    deferralReason.append("Recent surgery. ")
                    isEligible = false
                }
                if (pregnant) {
                    deferralReason.append("Pregnancy or recent pregnancy. ")
                    isEligible = false
                }

                val updatedHealth = currentDonorHealth.copy(
                    hemoglobinLevel = hemoglobin,
                    bloodPressureSystolic = systolic,
                    bloodPressureDiastolic = diastolic,
                    weight = weight,
                    temperature = temperature,
                    pulseRate = pulseRate,
                    feelingWell = feelingWell,
                    takenMedication = takenMedication,
                    traveled = traveled,
                    hadSurgery = hadSurgery,
                    pregnant = pregnant,
                    lastHealthStatus = if (isEligible) "ELIGIBLE" else "DEFERRED",
                    deferralReason = if (isEligible) null else deferralReason.toString().trim(),
                    lastUpdated = System.currentTimeMillis()
                )

                // Determine whether to create or update
                val result = if (_donorHealth.value == null) {
                    donorHealthRepository.createDonorHealth(currentUserId, updatedHealth).first() // Use first() to get immediate result
                } else {
                    donorHealthRepository.updateDonorHealth(currentUserId, updatedHealth).first() // Use first() to get immediate result
                }

                _updateResult.emit(result)
                _isLoading.value = false

                if (result is Result.Success) {
                    // Reload health data to reflect the changes (including eligibility based on nextEligibleDate)
                    loadDonorHealth()
                }

            } catch (e: NumberFormatException) {
                _error.emit("Please enter valid numbers for health metrics.")
                _isLoading.value = false
            } catch (e: Exception) {
                _error.emit("Failed to update health metrics: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculates the number of days until the donor is eligible to donate again.
     * This logic is now part of the ViewModel.
     */
    fun calculateDaysUntilEligible(donorHealth: DonorHealth?): Long {
        val health = donorHealth ?: return 0L
        if (health.lastDonationDate == 0L) {
            return 0L
        }

        val nextEligibleDate = health.lastDonationDate + DONATION_INTERVAL
        val currentTime = System.currentTimeMillis()

        return if (nextEligibleDate > currentTime) {
            TimeUnit.MILLISECONDS.toDays(nextEligibleDate - currentTime)
        } else {
            0L
        }
    }

    /**
     * Determines if the donor is currently eligible based on health status and donation interval.
     * This logic is now part of the ViewModel.
     */
    fun isEligibleToDonate(donorHealth: DonorHealth?): Boolean {
        val health = donorHealth ?: return false
        val intervalEligible = calculateDaysUntilEligible(health) == 0L
        val healthStatusEligible = health.lastHealthStatus == "ELIGIBLE"
        return intervalEligible && healthStatusEligible
    }
}