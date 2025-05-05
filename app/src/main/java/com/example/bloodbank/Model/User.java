package com.example.bloodbank.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String birthdate;
    private String bloodGroup;
    private String type;
    private String password;
    private String occupation;
    private String lastDonationDate;
    private String profileImagePath;
    private String gender;
    private String idnumber;
    private String search;
    private String hospitalAddress;
    private String patientName;
    private int requiredUnits;
    private String urgencyLevel;
    private double weight;
    private double height;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String id, String name, String email, String phoneNumber, String address, 
                String birthdate, String bloodGroup, String type, String password, 
                String occupation, String lastDonationDate, String profileImagePath,
                String gender, String idnumber, String search, String hospitalAddress,
                String patientName, int requiredUnits, String urgencyLevel,
                double weight, double height) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.birthdate = birthdate;
        this.bloodGroup = bloodGroup;
        this.type = type;
        this.password = password;
        this.occupation = occupation;
        this.lastDonationDate = lastDonationDate;
        this.profileImagePath = profileImagePath;
        this.gender = gender;
        this.idnumber = idnumber;
        this.search = search;
        this.hospitalAddress = hospitalAddress;
        this.patientName = patientName;
        this.requiredUnits = requiredUnits;
        this.urgencyLevel = urgencyLevel;
        this.weight = weight;
        this.height = height;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @com.google.firebase.database.PropertyName("phoneNumber")
    public String getPhoneNumber() { return phoneNumber; }
    @com.google.firebase.database.PropertyName("phoneNumber")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    @com.google.firebase.database.PropertyName("bloodGroup")
    public String getBloodGroup() { return bloodGroup; }
    @com.google.firebase.database.PropertyName("bloodGroup")
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    @com.google.firebase.database.PropertyName("lastDonationDate")
    public String getLastDonationDate() { return lastDonationDate; }
    @com.google.firebase.database.PropertyName("lastDonationDate")
    public void setLastDonationDate(String lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    @com.google.firebase.database.PropertyName("profileImagePath")
    public String getProfileImagePath() { return profileImagePath; }
    @com.google.firebase.database.PropertyName("profileImagePath")
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getIdnumber() { return idnumber; }
    public void setIdnumber(String idnumber) { this.idnumber = idnumber; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public int getRequiredUnits() { return requiredUnits; }
    public void setRequiredUnits(int requiredUnits) { this.requiredUnits = requiredUnits; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
