package com.example.bloodbank.Model;

public class CompatibleUser {
    private String userId;
    private String name;
    private String type;
    private String bloodGroup;
    private String profilePictureUrl;
    private String email;
    private String phoneNumber;
    private String address;
    private String lastDonation;

    // Default constructor for Firebase
    public CompatibleUser() {
    }

    public CompatibleUser(String userId, String name, String type, String bloodGroup, String profilePictureUrl, String email, String phoneNumber, String address, String lastDonation) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.bloodGroup = bloodGroup;
        this.profilePictureUrl = profilePictureUrl;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.lastDonation = lastDonation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLastDonation() {
        return lastDonation;
    }

    public void setLastDonation(String lastDonation) {
        this.lastDonation = lastDonation;
    }
} 