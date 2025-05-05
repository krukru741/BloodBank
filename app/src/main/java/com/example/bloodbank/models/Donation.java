package com.example.bloodbank.models;

public class Donation {
    private String donationId;
    private String donorId;
    private String bloodGroup;
    private String donationDate;
    private String donationCenter;
    private String status; // scheduled, completed, cancelled
    private String emergencyRequestId; // if donation is for an emergency request
    private int units;
    private String notes;
    private long timestamp;

    // Default constructor required for Firebase
    public Donation() {
        this.timestamp = System.currentTimeMillis();
    }

    public Donation(String donationId, String donorId, String bloodGroup, String donationDate,
                   String donationCenter, int units) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.bloodGroup = bloodGroup;
        this.donationDate = donationDate;
        this.donationCenter = donationCenter;
        this.units = units;
        this.status = "scheduled";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(String donationDate) {
        this.donationDate = donationDate;
    }

    public String getDonationCenter() {
        return donationCenter;
    }

    public void setDonationCenter(String donationCenter) {
        this.donationCenter = donationCenter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmergencyRequestId() {
        return emergencyRequestId;
    }

    public void setEmergencyRequestId(String emergencyRequestId) {
        this.emergencyRequestId = emergencyRequestId;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 