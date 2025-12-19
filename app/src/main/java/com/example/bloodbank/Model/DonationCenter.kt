package com.example.bloodbank.Model

data class DonationCenter(
    var centerId: String? = null,
    var name: String? = null,
    var address: String? = null,
    var city: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var operatingHours: String? = null,
    var maxDailyAppointments: Int = 0,
    var isActive: Boolean = false,
    var createdAt: Long? = null
)