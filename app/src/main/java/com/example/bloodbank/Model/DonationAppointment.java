package com.example.bloodbank.Model;

import java.util.Date;

public class DonationAppointment {
    private String appointmentId;
    private String donorId;
    private String centerId;
    private long appointmentDate;
    private String timeSlot;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private String notes;
    private long createdAt;
    private long lastUpdated;

    public DonationAppointment() {
        // Required empty constructor for Firebase
    }

    public DonationAppointment(String appointmentId, String donorId, String centerId,
            long appointmentDate, String timeSlot, String status, String notes) {
        this.appointmentId = appointmentId;
        this.donorId = donorId;
        this.centerId = centerId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = status;
        this.notes = notes;
        this.createdAt = new Date().getTime();
        this.lastUpdated = this.createdAt;
    }

    // Getters and Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public long getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(long appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.lastUpdated = new Date().getTime();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.lastUpdated = new Date().getTime();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}