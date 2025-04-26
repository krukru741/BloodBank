package com.example.bloodbank.Model;

public class DonationCenter {
    private String centerId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private double latitude;
    private double longitude;
    private String operatingHours;
    private int maxDailyAppointments;
    private boolean isActive;

    public DonationCenter() {
        // Required empty constructor for Firebase
    }

    public DonationCenter(String centerId, String name, String address, String phone,
            String email, double latitude, double longitude, String operatingHours,
            int maxDailyAppointments, boolean isActive) {
        this.centerId = centerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours;
        this.maxDailyAppointments = maxDailyAppointments;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public int getMaxDailyAppointments() {
        return maxDailyAppointments;
    }

    public void setMaxDailyAppointments(int maxDailyAppointments) {
        this.maxDailyAppointments = maxDailyAppointments;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}