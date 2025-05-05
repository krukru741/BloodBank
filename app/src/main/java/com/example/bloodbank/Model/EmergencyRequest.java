package com.example.bloodbank.Model;

import java.util.Map;

public class EmergencyRequest {
    private String requestId;
    private String userId;
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalContact;
    private String hospitalContactNumber;
    private String patientName;
    private String bloodGroup;
    private String unitsNeeded;
    private String emergencyDetails;
    private String status;
    private long timestamp;
    private Double latitude;
    private Double longitude;
    private String requestedBy;
    private String rejectedBy;
    private String priorityLevel;
    private String priorityDescription;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Map<String, EmergencyResponse> responses;
    private String acceptedDonorId;

    public EmergencyRequest() {
        // Required empty constructor for Firebase
    }

    public EmergencyRequest(String requestId, String userId, String hospitalName,
            String hospitalAddress, String hospitalContact, String patientName,
            String bloodGroup, String unitsNeeded, String emergencyDetails,
            String status, long timestamp, Double latitude, Double longitude,
            String requestedBy, String priorityLevel) {
        this.requestId = requestId;
        this.userId = userId;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.hospitalContact = hospitalContact;
        this.patientName = patientName;
        this.bloodGroup = bloodGroup;
        this.unitsNeeded = unitsNeeded;
        this.emergencyDetails = emergencyDetails;
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.requestedBy = requestedBy;
        this.priorityLevel = priorityLevel;
    }

    // Getters and Setters with type conversion
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(Object requestId) {
        this.requestId = requestId != null ? requestId.toString() : null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(Object userId) {
        this.userId = userId != null ? userId.toString() : null;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(Object hospitalName) {
        this.hospitalName = hospitalName != null ? hospitalName.toString() : null;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(Object hospitalAddress) {
        this.hospitalAddress = hospitalAddress != null ? hospitalAddress.toString() : null;
    }

    public String getHospitalContact() {
        return hospitalContact;
    }

    public void setHospitalContact(Object hospitalContact) {
        this.hospitalContact = hospitalContact != null ? hospitalContact.toString() : null;
    }

    public String getHospitalContactNumber() {
        return hospitalContactNumber;
    }

    public void setHospitalContactNumber(Object hospitalContactNumber) {
        this.hospitalContactNumber = hospitalContactNumber != null ? hospitalContactNumber.toString() : null;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(Object patientName) {
        this.patientName = patientName != null ? patientName.toString() : null;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(Object bloodGroup) {
        this.bloodGroup = bloodGroup != null ? bloodGroup.toString() : null;
    }

    public String getUnitsNeeded() {
        return unitsNeeded;
    }

    public void setUnitsNeeded(Object unitsNeeded) {
        this.unitsNeeded = unitsNeeded != null ? unitsNeeded.toString() : null;
    }

    public String getEmergencyDetails() {
        return emergencyDetails;
    }

    public void setEmergencyDetails(Object emergencyDetails) {
        this.emergencyDetails = emergencyDetails != null ? emergencyDetails.toString() : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status != null ? status.toString() : null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        } else if (timestamp instanceof Double) {
            this.timestamp = ((Double) timestamp).longValue();
        } else if (timestamp != null) {
            try {
                this.timestamp = Long.parseLong(timestamp.toString());
            } catch (NumberFormatException e) {
                this.timestamp = 0;
            }
        } else {
            this.timestamp = 0;
        }
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Object latitude) {
        if (latitude instanceof Double) {
            this.latitude = (Double) latitude;
        } else if (latitude != null) {
            try {
                this.latitude = Double.parseDouble(latitude.toString());
            } catch (NumberFormatException e) {
                this.latitude = null;
            }
        } else {
            this.latitude = null;
        }
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Object longitude) {
        if (longitude instanceof Double) {
            this.longitude = (Double) longitude;
        } else if (longitude != null) {
            try {
                this.longitude = Double.parseDouble(longitude.toString());
            } catch (NumberFormatException e) {
                this.longitude = null;
            }
        } else {
            this.longitude = null;
        }
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Object requestedBy) {
        this.requestedBy = requestedBy != null ? requestedBy.toString() : null;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(Object rejectedBy) {
        this.rejectedBy = rejectedBy != null ? rejectedBy.toString() : null;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Object priorityLevel) {
        this.priorityLevel = priorityLevel != null ? priorityLevel.toString() : null;
    }

    public String getPriorityDescription() {
        return priorityDescription;
    }

    public void setPriorityDescription(Object priorityDescription) {
        this.priorityDescription = priorityDescription != null ? priorityDescription.toString() : null;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(Object emergencyContactName) {
        this.emergencyContactName = emergencyContactName != null ? emergencyContactName.toString() : null;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(Object emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone != null ? emergencyContactPhone.toString() : null;
    }

    public Map<String, EmergencyResponse> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, EmergencyResponse> responses) {
        this.responses = responses;
    }

    public String getAcceptedDonorId() {
        return acceptedDonorId;
    }

    public void setAcceptedDonorId(Object acceptedDonorId) {
        this.acceptedDonorId = acceptedDonorId != null ? acceptedDonorId.toString() : null;
    }
}