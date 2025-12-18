package com.example.bloodbank.Model

data class EmergencyResponse(
    var donorId: String? = null,
    var status: String? = null,
    var timestamp: Long = 0L,
    var message: String? = null
)