package com.example.bloodbank

import android.app.Application
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.Model.EmergencyResponse
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.EmergencyRequestRepository
import com.example.bloodbank.repository.UserRepository
import com.example.bloodbank.repository.Result
import com.example.bloodbank.Util.LocationHelper
import com.example.bloodbank.Util.NotificationHelper
import com.google.firebase.database.ServerValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class EmergencyRequestViewModel @Inject constructor(
    private val emergencyRequestRepository: EmergencyRequestRepository,
    private val userRepository: UserRepository,
    private val smsManager: SmsManager,
    private val application: Application // For NotificationHelper and general context needs
) : ViewModel() {

    companion object {
        val BLOOD_GROUPS = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val PRIORITY_LEVELS = arrayOf("Normal", "Urgent", "Critical")
        private const val NOTIFICATION_TOPIC = "emergency_requests"
        private const val TAG = "EmergencyRequestVM"
    }

    private val _requestStatus = MutableSharedFlow<Result<Unit>>()
    val requestStatus: SharedFlow<Result<Unit>> = _requestStatus.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    fun submitEmergencyRequest(
        hospitalName: String,
        hospitalAddress: String,
        hospitalContact: String,
        patientName: String,
        bloodGroup: String,
        priorityLevel: String,
        unitsNeeded: String,
        emergencyDetails: String,
        location: Location?
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // Input validation
            if (hospitalName.isBlank() || hospitalAddress.isBlank() || hospitalContact.isBlank() ||
                patientName.isBlank() || bloodGroup.isBlank() || priorityLevel.isBlank() ||
                unitsNeeded.isBlank() || emergencyDetails.isBlank()
            ) {
                _error.emit("Please fill in all fields")
                _isLoading.value = false
                return@launch
            }

            val currentUserId = userRepository.getCurrentUserUid()
            if (currentUserId == null) {
                _error.emit("User not logged in.")
                _isLoading.value = false
                return@launch
            }

            val requestId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            val emergencyRequest = EmergencyRequest(
                requestId = requestId,
                userId = currentUserId,
                hospitalName = hospitalName,
                hospitalAddress = hospitalAddress,
                hospitalContact = hospitalContact,
                hospitalContactNumber = hospitalContact, // Using hospitalContact as hospitalContactNumber for now
                patientName = patientName,
                bloodGroup = bloodGroup,
                priorityLevel = priorityLevel,
                unitsNeeded = unitsNeeded,
                emergencyDetails = emergencyDetails,
                status = "ACTIVE",
                timestamp = timestamp,
                latitude = location?.latitude,
                longitude = location?.longitude,
                requestedBy = currentUserId,
                rejectedBy = null,
                priorityDescription = null, // Can be set based on priorityLevel if needed
                emergencyContactName = null, // Not collected in UI
                emergencyContactPhone = null, // Not collected in UI
                responses = null,
                acceptedDonorId = null
            )

            emergencyRequestRepository.createEmergencyRequest(emergencyRequest).collect { result ->
                when (result) {
                    is Result.Success -> {
                        sendEmergencySMS(hospitalContact, hospitalName, bloodGroup)
                        notifyNearbyDonors(requestId, bloodGroup, location)
                        _requestStatus.emit(Result.Success(Unit))
                    }
                    is Result.Error -> {
                        _error.emit("Failed to submit request: ${result.exception.message}")
                    }
                }
                _isLoading.value = false
            }
        }
    }

    private fun sendEmergencySMS(phoneNumber: String, hospitalName: String, bloodGroup: String) {
        // SMS permission check is handled by the Activity.
        // We assume here that if this function is called, the permission is granted.
        try {
            val message = "EMERGENCY: Blood donation needed at $hospitalName " +
                    "for blood group $bloodGroup. Please respond ASAP."
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            viewModelScope.launch {
                _error.emit("Failed to send emergency SMS: ${e.message}")
            }
        }
    }

    private fun notifyNearbyDonors(requestId: String, bloodGroup: String, requestLocation: Location?) {
        viewModelScope.launch {
            if (requestLocation == null) {
                // If location is null, notify all compatible donors
                userRepository.readDonors().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data.forEach { donor ->
                                if (isCompatibleBloodGroup(bloodGroup, donor.bloodGroup)) {
                                    sendNotificationToDonor(donor, requestId, bloodGroup)
                                }
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Failed to fetch all donors for notification: ${result.exception.message}")
                            _error.emit("Failed to notify all donors: ${result.exception.message}")
                        }
                    }
                }
                return@launch
            }

            // Notify nearby compatible donors
            userRepository.readDonors().collect { result ->
                when (result) {
                    is Result.Success -> {
                        result.data.forEach { donor ->
                            if (isCompatibleBloodGroup(bloodGroup, donor.bloodGroup)) {
                                if (donor.latitude != null && donor.longitude != null) {
                                    if (LocationHelper.isWithinRange(
                                            requestLocation.latitude,
                                            requestLocation.longitude,
                                            donor.latitude,
                                            donor.longitude
                                        )
                                    ) {
                                        sendNotificationToDonor(donor, requestId, bloodGroup)
                                    }
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to fetch nearby donors for notification: ${result.exception.message}")
                        _error.emit("Failed to notify nearby donors: ${result.exception.message}")
                    }
                }
            }
        }
    }

    private fun sendNotificationToDonor(donor: User, requestId: String, bloodGroup: String) {
        val title = "Emergency Blood Request"
        val message = "Urgent need for $bloodGroup blood. Can you help?"

        // Send notification using NotificationHelper with priority level 3 (Critical)
        // NotificationHelper is a static utility, so we pass context
        NotificationHelper.sendEmergencyNotification(application, title, message, requestId, 3)

        // Store notification in Firebase
        val notificationId = UUID.randomUUID().toString()
        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "requestId" to requestId,
            "timestamp" to ServerValue.TIMESTAMP, // Use ServerValue.TIMESTAMP for Firebase server timestamp
            "read" to false
        )
        // Directly using DatabaseHelper here as no dedicated NotificationRepository yet
        userRepository.getNotificationsReference(donor.id ?: return)
            .child(notificationId)
            .setValue(notificationData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Notification stored for donor: ${donor.id}")
                } else {
                    Log.e(TAG, "Failed to store notification for donor: ${donor.id}, ${task.exception?.message}")
                    viewModelScope.launch {
                        _error.emit("Failed to store notification for donor: ${donor.id}")
                    }
                }
            }
    }

    private fun isCompatibleBloodGroup(requestedBloodGroup: String, donorBloodGroup: String?): Boolean {
        if (donorBloodGroup == null) return false

        // Blood group compatibility logic
        return when (requestedBloodGroup) {
            "A+" -> donorBloodGroup == "A+" || donorBloodGroup == "A-" ||
                    donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "A-" -> donorBloodGroup == "A-" || donorBloodGroup == "O-"
            "B+" -> donorBloodGroup == "B+" || donorBloodGroup == "B-" ||
                    donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "B-" -> donorBloodGroup == "B-" || donorBloodGroup == "O-"
            "AB+" -> true // Can receive from all blood groups
            "AB-" -> donorBloodGroup.endsWith("-") // Can receive from all negative blood groups
            "O+" -> donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "O-" -> donorBloodGroup == "O-"
            else -> false
        }
    }
}