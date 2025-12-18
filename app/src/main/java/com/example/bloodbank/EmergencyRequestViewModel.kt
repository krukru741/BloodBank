package com.example.bloodbank

import android.app.Application
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.Model.User
import com.example.bloodbank.repository.EmergencyRequestRepository
import com.example.bloodbank.repository.UserRepository
import com.example.bloodbank.repository.Result
import com.example.bloodbank.Util.LocationHelper
import com.example.bloodbank.Util.NotificationHelper
import com.example.bloodbank.util.Event
import com.google.firebase.auth.FirebaseAuth
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
    private val firebaseAuth: FirebaseAuth,
    private val smsManager: SmsManager,
    private val application: Application // For NotificationHelper and general context needs
) : ViewModel() {

    companion object {
        val BLOOD_GROUPS = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val PRIORITY_LEVELS = arrayOf("Normal", "Urgent", "Critical")
        private const val TAG = "EmergencyRequestVM"
    }

    // UI State for loading, errors, and success
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _requestCreationSuccess = MutableSharedFlow<Unit>()
    val requestCreationSuccess: SharedFlow<Unit> = _requestCreationSuccess.asSharedFlow()

    // Events for triggering actions in the Activity
    private val _permissionEvent = MutableSharedFlow<Event<List<String>>>()
    val permissionEvent: SharedFlow<Event<List<String>>> = _permissionEvent.asSharedFlow()

    private val _notificationEvent = MutableSharedFlow<Event<Triple<User, String, String>>>()
    val notificationEvent: SharedFlow<Event<Triple<User, String, String>>> = _notificationEvent.asSharedFlow()

    private val _smsEvent = MutableSharedFlow<Event<Triple<String, String, String>>>() // phoneNumber, hospitalName, bloodGroup
    val smsEvent: SharedFlow<Event<Triple<String, String, String>>> = _smsEvent.asSharedFlow()

    // Function to submit an emergency request
    fun createEmergencyRequest(
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
                _errorMessage.emit("Please fill in all fields")
                _isLoading.value = false
                return@launch
            }

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                _errorMessage.emit("User not logged in.")
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
                        _requestCreationSuccess.emit(Unit)
                        // Trigger SMS and Notification events for the Activity to handle
                        _smsEvent.emit(Event(Triple(hospitalContact, hospitalName, bloodGroup)))
                        notifyNearbyDonors(requestId, bloodGroup, location) // This will trigger _notificationEvent
                    }
                    is Result.Error -> {
                        _errorMessage.emit("Failed to submit request: ${result.exception.message}")
                    }
                }
                _isLoading.value = false
            }
        }
    }

    // Function to trigger SMS sending (called by Activity based on _smsEvent)
    fun sendEmergencySMS(phoneNumber: String, hospitalName: String, bloodGroup: String) {
        try {
            val message = "EMERGENCY: Blood donation needed at $hospitalName " +
                    "for blood group $bloodGroup. Please respond ASAP."
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            viewModelScope.launch {
                _errorMessage.emit("Failed to send emergency SMS: ${e.message}")
            }
        }
    }

    // Function to handle notifying nearby donors
    private fun notifyNearbyDonors(requestId: String, bloodGroup: String, requestLocation: Location?) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.readDonors().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val eligibleDonors = result.data.filter { donor ->
                            isCompatibleBloodGroup(bloodGroup, donor.bloodGroup) &&
                                    (requestLocation == null ||
                                            (donor.latitude != null && donor.longitude != null &&
                                                    LocationHelper.isWithinRange(
                                                        requestLocation.latitude,
                                                        requestLocation.longitude,
                                                        donor.latitude,
                                                        donor.longitude
                                                    )))
                        }
                        eligibleDonors.forEach { donor ->
                            _notificationEvent.emit(Event(Triple(donor, requestId, bloodGroup)))
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to fetch donors for notification: ${result.exception.message}")
                        _errorMessage.emit("Failed to notify donors: ${result.exception.message}")
                    }
                }
                _isLoading.value = false
            }
        }
    }

    // Function to send a notification to a specific donor (called by Activity based on _notificationEvent)
    fun sendNotificationToDonor(donor: User, requestId: String, bloodGroup: String) {
        val title = "Emergency Blood Request"
        val message = "Urgent need for $bloodGroup blood. Can you help?"

        // Send notification using NotificationHelper (static utility)
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
        // Directly using DatabaseHelper via userRepository's internal DatabaseHelper reference for notifications
        // This implicitly assumes UserRepository can provide refs to generic user-related data like notifications
        // For a cleaner architecture, a dedicated NotificationRepository might be introduced later.
        val dbHelper = emergencyRequestRepository.getDatabaseHelper() // Assuming this method exists or is added for access
        dbHelper.getNotificationsReference().child(donor.id ?: return)
            .child(notificationId)
            .setValue(notificationData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Notification stored for donor: ${donor.id}")
                } else {
                    Log.e(TAG, "Failed to store notification for donor: ${donor.id}, ${task.exception?.message}")
                    viewModelScope.launch {
                        _errorMessage.emit("Failed to store notification for donor: ${donor.id}")
                    }
                }
            }
    }

    // Encapsulated blood group compatibility logic
    fun isCompatibleBloodGroup(requestedBloodGroup: String, donorBloodGroup: String?): Boolean {
        if (donorBloodGroup == null) return false

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