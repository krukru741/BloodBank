package com.example.bloodbank.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DonorHealth(
    var donorId: String? = null,
    var lastDonationDate: Long = 0L,
    var nextEligibleDate: Long = 0L, // This will be calculated elsewhere if needed for display
    var hemoglobinLevel: Double = 0.0,
    var bloodPressureSystolic: Int = 0,
    var bloodPressureDiastolic: Int = 0,
    var weight: Double = 0.0,
    var temperature: Double = 0.0,
    var pulseRate: Int = 0,
    var feelingWell: Boolean = false,
    var takenMedication: Boolean = false,
    var traveled: Boolean = false,
    var hadSurgery: Boolean = false,
    var pregnant: Boolean = false,
    var lastHealthStatus: String? = null, // ELIGIBLE, INELIGIBLE, TEMPORARY_DEFERRAL
    var deferralReason: String? = null,
    var lastUpdated: Long = 0L,
    var totalDonations: Int = 0
)