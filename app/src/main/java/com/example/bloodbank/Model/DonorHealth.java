package com.example.bloodbank.Model;

import java.util.Date;

public class DonorHealth {
    private String donorId;
    private long lastDonationDate;
    private long nextEligibleDate;
    private double hemoglobinLevel;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private double weight;
    private String lastHealthStatus; // ELIGIBLE, INELIGIBLE, TEMPORARY_DEFERRAL
    private String deferralReason;
    private long lastUpdated;
    private int totalDonations;

    public DonorHealth() {
        // Required empty constructor for Firebase
        this.lastHealthStatus = "Not Evaluated";
        this.totalDonations = 0;
    }

    public DonorHealth(String donorId) {
        this.donorId = donorId;
        this.lastDonationDate = 0;
        this.nextEligibleDate = 0;
        this.totalDonations = 0;
        this.lastHealthStatus = "ELIGIBLE";
        this.lastUpdated = new Date().getTime();
    }

    // Getters and Setters
    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public long getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(long lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
        // Update next eligible date (56 days from last donation)
        this.nextEligibleDate = lastDonationDate + (56L * 24 * 60 * 60 * 1000);
        this.lastUpdated = new Date().getTime();
    }

    public long getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(long nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public double getHemoglobinLevel() {
        return hemoglobinLevel;
    }

    public void setHemoglobinLevel(double hemoglobinLevel) {
        this.hemoglobinLevel = hemoglobinLevel;
        this.lastUpdated = new Date().getTime();
    }

    public int getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(int bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
        this.lastUpdated = new Date().getTime();
    }

    public int getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(int bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
        this.lastUpdated = new Date().getTime();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
        this.lastUpdated = new Date().getTime();
    }

    public String getLastHealthStatus() {
        return lastHealthStatus;
    }

    public void setLastHealthStatus(String lastHealthStatus) {
        this.lastHealthStatus = lastHealthStatus;
        this.lastUpdated = new Date().getTime();
    }

    public String getDeferralReason() {
        return deferralReason;
    }

    public void setDeferralReason(String deferralReason) {
        this.deferralReason = deferralReason;
        this.lastUpdated = new Date().getTime();
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(int totalDonations) {
        this.totalDonations = totalDonations;
        this.lastUpdated = new Date().getTime();
    }

    public void incrementTotalDonations() {
        this.totalDonations++;
        this.lastUpdated = new Date().getTime();
    }

    public boolean isEligibleToDonate() {
        return "ELIGIBLE".equals(lastHealthStatus) &&
                System.currentTimeMillis() >= nextEligibleDate;
    }

    public long getDaysUntilEligible() {
        if (isEligibleToDonate())
            return 0;
        long diff = nextEligibleDate - System.currentTimeMillis();
        return diff > 0 ? diff / (24 * 60 * 60 * 1000) : 0;
    }
}