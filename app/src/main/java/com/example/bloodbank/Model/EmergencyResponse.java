package com.example.bloodbank.Model;

public class EmergencyResponse {
    private String donorId;
    private String status;
    private long timestamp;
    private String message;

    public EmergencyResponse() {
        // Required empty constructor for Firebase
    }

    public EmergencyResponse(String donorId, String status, long timestamp, String message) {
        this.donorId = donorId;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 