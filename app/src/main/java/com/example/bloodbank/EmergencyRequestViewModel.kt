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
import com.google.firebase.database.FirebaseDatabase
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
    private val application: Application
) : ViewModel() {

    companion object {
        val BLOOD_GROUPS = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val PRIORITY_LEVELS = arrayOf("Normal", "Urgent", "Critical")
        private const val TAG = "EmergencyRequestVM"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _requestCreationSuccess = MutableSharedFlow<Unit>()
    val requestCreationSuccess: SharedFlow<Unit> = _requestCreationSuccess.asSharedFlow()

    private val _notificationEvent = MutableSharedFlow<Event<Triple<User, String, String>>>()
    val notificationEvent: SharedFlow<Event<Triple<User, String, String>>> = _notificationEvent.asSharedFlow()

    private val _smsEvent = MutableSharedFlow<Event<Triple<String, String, String>>>()
    val smsEvent: SharedFlow<Event<Triple<String, String, String>>> = _smsEvent.asSharedFlow()

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

            val emergencyRequest = EmergencyRequest(
                requestId = requestId,
                userId = currentUserId,
                hospitalName = hospitalName,
                hospitalAddress = hospitalAddress,
                hospitalContact = hospitalContact,
                hospitalContactNumber = hospitalContact,
                patientName = patientName,
                bloodGroup = bloodGroup,
                priorityLevel = priorityLevel,
                unitsNeeded = unitsNeeded,
                emergencyDetails = emergencyDetails,
                status = "ACTIVE",
                timestamp = System.currentTimeMillis(),
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                requestedBy = currentUserId,
                rejectedBy = null,
                priorityDescription = null,
                emergencyContactName = null,
                emergencyContactPhone = null,
                responses = null,
                acceptedDonorId = null
            )

            emergencyRequestRepository.createEmergencyRequest(emergencyRequest).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _requestCreationSuccess.emit(Unit)
                        _smsEvent.emit(Event(Triple(hospitalContact, hospitalName, bloodGroup)))
                        notifyNearbyDonors(requestId, bloodGroup, location)
                    }
                    is Result.Error -> {
                        _errorMessage.emit("Failed to submit request: ${result.exception.message}")
                    }
                }
                _isLoading.value = false
            }
        }
    }

    private fun notifyNearbyDonors(requestId: String, bloodGroup: String, requestLocation: Location?) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.readDonors().collect { result ->
                val donors = result
                val eligibleDonors = donors.filter { donor ->
                    val donorLat = donor.latitude
                    val donorLng = donor.longitude
                    isCompatibleBloodGroup(bloodGroup, donor.bloodGroup) &&
                            (requestLocation == null ||
                                    (donorLat != null && donorLng != null &&
                                            LocationHelper.isWithinRange(
                                                requestLocation.latitude,
                                                requestLocation.longitude,
                                                donorLat,
                                                donorLng
                                            )))
                }
                eligibleDonors.forEach { donor ->
                    _notificationEvent.emit(Event(Triple(donor, requestId, bloodGroup)))
                }
                _isLoading.value = false
            }
        }
    }

    fun sendNotificationToDonor(donor: User, requestId: String, bloodGroup: String) {
        val title = "Emergency Blood Request"
        val message = "Urgent need for $bloodGroup blood. Can you help?"

        NotificationHelper.sendEmergencyNotification(application, title, message, requestId, 3)

        val notificationId = UUID.randomUUID().toString()
        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "requestId" to requestId,
            "timestamp" to ServerValue.TIMESTAMP,
            "read" to false
        )
        
        FirebaseDatabase.getInstance().getReference("notifications")
            .child(donor.id ?: return)
            .child(notificationId)
            .setValue(notificationData)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "Failed to store notification for donor: ${donor.id}")
                }
            }
    }

    fun isCompatibleBloodGroup(requestedBloodGroup: String, donorBloodGroup: String?): Boolean {
        if (donorBloodGroup == null) return false

        return when (requestedBloodGroup) {
            "A+" -> donorBloodGroup == "A+" || donorBloodGroup == "A-" ||
                    donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "A-" -> donorBloodGroup == "A-" || donorBloodGroup == "O-"
            "B+" -> donorBloodGroup == "B+" || donorBloodGroup == "B-" ||
                    donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "B-" -> donorBloodGroup == "B-" || donorBloodGroup == "O-"
            "AB+" -> true
            "AB-" -> donorBloodGroup.endsWith("-")
            "O+" -> donorBloodGroup == "O+" || donorBloodGroup == "O-"
            "O-" -> donorBloodGroup == "O-"
            else -> false
        }
    }
}