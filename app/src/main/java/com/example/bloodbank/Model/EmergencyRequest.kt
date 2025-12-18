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
    var unitsNeeded: String? = null,
    var emergencyDetails: String? = null,
    var status: String? = null,
    var timestamp: Long? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var requestedBy: String? = null,
    var rejectedBy: String? = null,
    var priorityLevel: String? = null,
    var priorityDescription: String? = null,
    var emergencyContactName: String? = null,
    var emergencyContactPhone: String? = null,
    var responses: Map<String, EmergencyResponse>? = null, // Assuming EmergencyResponse will also be a data class
    var acceptedDonorId: String? = null
)
