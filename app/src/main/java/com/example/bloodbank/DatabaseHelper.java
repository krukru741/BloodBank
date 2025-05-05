package com.example.bloodbank;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseHelper {
    private static DatabaseHelper instance;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;

    private DatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // Users Collection
    public DatabaseReference getUsersReference() {
        return databaseReference.child("users");
    }

    public DatabaseReference getUserReference(String userId) {
        return getUsersReference().child(userId);
    }

    public DatabaseReference getUserProfileReference(String userId) {
        return getUserReference(userId).child("profile");
    }

    public DatabaseReference getUserHealthReference(String userId) {
        return getUserReference(userId).child("health");
    }

    public DatabaseReference getUserAchievementsReference(String userId) {
        return getUserReference(userId).child("achievements");
    }

    // Emergency Requests Collection
    public DatabaseReference getEmergencyRequestsReference() {
        return databaseReference.child("emergency_requests");
    }

    public DatabaseReference getEmergencyRequestReference(String requestId) {
        return getEmergencyRequestsReference().child(requestId);
    }

    public DatabaseReference getEmergencyRequestDetailsReference(String requestId) {
        return getEmergencyRequestReference(requestId).child("details");
    }

    public DatabaseReference getEmergencyRequestStatusReference(String requestId) {
        return getEmergencyRequestReference(requestId).child("status");
    }

    public DatabaseReference getEmergencyRequestResponsesReference(String requestId) {
        return getEmergencyRequestReference(requestId).child("responses");
    }

    // Notifications Collection
    public DatabaseReference getNotificationsReference() {
        return databaseReference.child("notifications");
    }

    public DatabaseReference getUserNotificationsReference(String userId) {
        return getNotificationsReference().child(userId);
    }

    public DatabaseReference getNotificationReference(String userId, String notificationId) {
        return getUserNotificationsReference(userId).child(notificationId);
    }

    // Donations Collection
    public DatabaseReference getDonationsReference() {
        return databaseReference.child("donations");
    }

    public DatabaseReference getDonorDonationsReference(String donorId) {
        return getDonationsReference().child(donorId);
    }

    public DatabaseReference getDonationReference(String donorId, String donationId) {
        return getDonorDonationsReference(donorId).child(donationId);
    }

    // Storage References
    public StorageReference getProfileImagesReference() {
        return storageReference.child("profile_images");
    }

    public StorageReference getUserProfileImageReference(String userId) {
        return getProfileImagesReference().child(userId);
    }
} 