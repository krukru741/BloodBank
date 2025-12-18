package com.example.bloodbank.Model

import androidx.annotation.Keep

/**
 * Compatible user data model for matching donors and recipients.
 * Represents a user who is compatible for blood donation/reception.
 */
@Keep
data class CompatibleUser(
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val bloodGroup: String = "",
    val profilePictureUrl: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val lastDonation: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "")
}
