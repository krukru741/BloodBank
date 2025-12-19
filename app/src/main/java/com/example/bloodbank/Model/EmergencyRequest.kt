package com.example.bloodbank.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class EmergencyRequest(
    var requestId: String? = null,
    var userId: String? = null,
    var hospitalName: String? = null,
    var hospitalAddress: String? = null,
    var hospitalContact: String? = null,
    var hospitalContactNumber: String? = null,
    var patientName: String? = null,
    var bloodGroup: String? = null,
    var priorityLevel: String? = null,
    var unitsNeeded: String? = null,
    var emergencyDetails: String? = null,
    var status: String? = "ACTIVE",
    var timestamp: Long = System.currentTimeMillis(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    var requestedBy: String? = null,
    var rejectedBy: List<String>? = null,
    var priorityDescription: String? = null,
    var emergencyContactName: String? = null,
    var emergencyContactPhone: String? = null,
    var responses: Map<String, Boolean>? = null,
    var acceptedDonorId: String? = null
) {
    // Aliases for backward compatibility
    val hospital: String?
        get() = hospitalName

    var urgencyLevel: String?
        get() = priorityLevel
        set(value) { priorityLevel = value }
}