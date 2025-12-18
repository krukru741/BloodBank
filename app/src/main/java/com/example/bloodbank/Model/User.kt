package com.example.bloodbank.Model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    @get:PropertyName("phoneNumber") @set:PropertyName("phoneNumber") var phoneNumber: String? = null,
    var address: String? = null,
    var birthdate: String? = null,
    @get:PropertyName("bloodGroup") @set:PropertyName("bloodGroup") var bloodGroup: String? = null,
    var type: String? = null,
    var password: String? = null,
    var occupation: String? = null,
    @get:PropertyName("lastDonationDate") @set:PropertyName("lastDonationDate") var lastDonationDate: String? = null,
    @get:PropertyName("profileImagePath") @set:PropertyName("profileImagePath") var profileImagePath: String? = null,
    var gender: String? = null,
    var idnumber: String? = null,
    var search: String? = null,
    var hospitalAddress: String? = null,
    var patientName: String? = null,
    var requiredUnits: Int = 0,
    var urgencyLevel: String? = null,
    var weight: Double = 0.0,
    var height: Double = 0.0
)