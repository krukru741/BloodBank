package com.example.bloodbank.models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Donation(
    var donationId: String? = null,
    var donorId: String? = null,
    var recipientId: String? = null,
    var donationCenterId: String? = null,
    var donationDate: Long? = null, // Using Long to store timestamp, matching `Date().getTime()` from Java
    var bloodGroup: String? = null,
    var status: String? = null, // e.g., "SCHEDULED", "COMPLETED", "CANCELLED"
    var notes: String? = null,
    var timestamp: Long? = null // For creation/last update time, using Long for consistency
)