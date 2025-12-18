package com.example.bloodbank.Model

data class DonationCenter(
    var id: String? = null,
    var name: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var city: String? = null,
    var createdAt: Long = 0L
)