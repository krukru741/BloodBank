package com.example.bloodbank.Model

data class EmergencyRequest(
    var requestId: String? = null,
    var patientName: String? = null,
    var bloodGroup: String? = null,
    var hospitalName: String? = null,
    var hospitalAddress: String? = null,
    var contactNumber: String? = null,
    var requiredDate: String? = null,
    var description: String? = null,
    var requesterId: String? = null,
    var status: String? = "pending",
    var createdAt: Long = System.currentTimeMillis(),
    var responses: Map<String, Boolean>? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)