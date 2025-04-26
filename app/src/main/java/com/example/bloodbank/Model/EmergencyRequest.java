package com.example.bloodbank.Model;

public class EmergencyRequest {
    private String requestId;
    private String userId;
    private String hospitalName;
    private String unitsNeeded;
    private String emergencyDetails;
    private String bloodGroup;
    private String status;
    private String timestamp;
    private Double latitude;
    private Double longitude;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String hospitalContactNumber;
    private int priorityLevel; // 1: Normal, 2: Urgent, 3: Critical
    private String priorityDescription;

    public EmergencyRequest() {
        // Required empty constructor for Firebase
    }

    public EmergencyRequest(String requestId, String userId, String hospitalName,
            String unitsNeeded, String emergencyDetails, String bloodGroup,
            String status, String timestamp, Double latitude, Double longitude,
            String emergencyContactName, String emergencyContactPhone,
            String hospitalContactNumber, int priorityLevel,
            String priorityDescription) {
        this.requestId = requestId;
        this.userId = userId;
        this.hospitalName = hospitalName;
        this.unitsNeeded = unitsNeeded;
        this.emergencyDetails = emergencyDetails;
        this.bloodGroup = bloodGroup;
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.hospitalContactNumber = hospitalContactNumber;
        this.priorityLevel = priorityLevel;
        this.priorityDescription = priorityDescription;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getUnitsNeeded() {
        return unitsNeeded;
    }

    public void setUnitsNeeded(String unitsNeeded) {
        this.unitsNeeded = unitsNeeded;
    }

    public String getEmergencyDetails() {
        return emergencyDetails;
    }

    public void setEmergencyDetails(String emergencyDetails) {
        this.emergencyDetails = emergencyDetails;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getHospitalContactNumber() {
        return hospitalContactNumber;
    }

    public void setHospitalContactNumber(String hospitalContactNumber) {
        this.hospitalContactNumber = hospitalContactNumber;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getPriorityDescription() {
        return priorityDescription;
    }

    public void setPriorityDescription(String priorityDescription) {
        this.priorityDescription = priorityDescription;
    }
}