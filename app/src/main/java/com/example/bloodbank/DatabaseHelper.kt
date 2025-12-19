package com.example.bloodbank

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseHelper @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) {
    private val databaseReference: DatabaseReference = firebaseDatabase.reference
    private val storageReference: StorageReference = firebaseStorage.reference

    // Users Collection
    fun getUsersReference(): DatabaseReference {
        return databaseReference.child("users")
    }

    fun getUserReference(userId: String): DatabaseReference {
        return getUsersReference().child(userId)
    }

    fun getUserProfileReference(userId: String): DatabaseReference {
        return getUserReference(userId).child("profile")
    }

    fun getUserHealthReference(userId: String): DatabaseReference {
        return getUserReference(userId).child("health")
    }

    fun getUserAchievementsReference(userId: String): DatabaseReference {
        return getUserReference(userId).child("achievements")
    }

    // Emergency Requests Collection
    fun getEmergencyRequestsReference(): DatabaseReference {
        return databaseReference.child("emergency_requests")
    }

    fun getEmergencyRequestReference(requestId: String): DatabaseReference {
        return getEmergencyRequestsReference().child(requestId)
    }

    fun getEmergencyRequestDetailsReference(requestId: String): DatabaseReference {
        return getEmergencyRequestReference(requestId).child("details")
    }

    fun getEmergencyRequestStatusReference(requestId: String): DatabaseReference {
        return getEmergencyRequestReference(requestId).child("status")
    }

    fun getEmergencyRequestResponsesReference(requestId: String): DatabaseReference {
        return getEmergencyRequestReference(requestId).child("responses")
    }

    // Notifications Collection
    fun getNotificationsReference(): DatabaseReference {
        return databaseReference.child("notifications")
    }

    fun getUserNotificationsReference(userId: String): DatabaseReference {
        return getNotificationsReference().child(userId)
    }

    fun getNotificationReference(userId: String, notificationId: String): DatabaseReference {
        return getUserNotificationsReference(userId).child(notificationId)
    }

    // Donations Collection
    fun getDonationsReference(): DatabaseReference {
        return databaseReference.child("donations")
    }

    fun getDonorDonationsReference(donorId: String): DatabaseReference {
        return getDonationsReference().child(donorId)
    }

    fun getDonationReference(donorId: String, donationId: String): DatabaseReference {
        return getDonorDonationsReference(donorId).child(donationId)
    }

    // Donation Centers Collection
    fun getDonationCentersReference(): DatabaseReference {
        return databaseReference.child("donation_centers")
    }

    fun getDonationCenterReference(centerId: String): DatabaseReference {
        return getDonationCentersReference().child(centerId)
    }

    // Storage References
    fun getProfileImagesReference(): StorageReference {
        return storageReference.child("profile_images")
    }

    fun getUserProfileImageReference(userId: String): StorageReference {
        return getProfileImagesReference().child(userId)
    }
}
