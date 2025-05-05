package com.example.bloodbank.models;

import java.util.HashMap;
import java.util.Map;

public class EmergencyRequest {
    private String requestId;
    private String patientName;
    private String bloodGroup;
    private String hospitalName;
    private String hospitalAddress;
    private String contactNumber;
    private String requiredDate;
    private String description;
    private String status; // pending, accepted, completed, cancelled
    private String requesterId;
    private long timestamp;
    private Map<String, Boolean> responses; // donorId -> true/false

    // Default constructor required for Firebase
    public EmergencyRequest() {
        this.responses = new HashMap<>();
    }

    public EmergencyRequest(String requestId, String patientName, String bloodGroup, String hospitalName,
                          String hospitalAddress, String contactNumber, String requiredDate,
                          String description, String requesterId) {
        this.requestId = requestId;
        this.patientName = patientName;
        this.bloodGroup = bloodGroup;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.contactNumber = contactNumber;
        this.requiredDate = requiredDate;
        this.description = description;
        this.requesterId = requesterId;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
        this.responses = new HashMap<>();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(String requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Boolean> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, Boolean> responses) {
        this.responses = responses;
    }

    public void addResponse(String donorId, boolean accepted) {
        if (responses == null) {
            responses = new HashMap<>();
        }
        responses.put(donorId, accepted);
    }
} 