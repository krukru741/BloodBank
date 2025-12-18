package com.example.bloodbank.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DonationAppointment(
    var appointmentId: String? = null,
    var donorId: String? = null,
    var centerId: String? = null,
    var appointmentDate: Long = 0L,
    var timeSlot: String? = null,
    var status: String? = null, // SCHEDULED, COMPLETED, CANCELLED
    var notes: String? = null,
    var createdAt: Long = 0L,
    var lastUpdated: Long = 0L
)
