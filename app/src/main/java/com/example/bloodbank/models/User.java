package com.example.bloodbank.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String bloodGroup;
    private String gender;
    private String birthDate;
    private String profileImageUrl;
    private String type; // donor or recipient
    private String search; // for search functionality

    // Default constructor required for Firebase
    public User() {}

    public User(String id, String name, String email, String phoneNumber, String address,
                String bloodGroup, String gender, String birthDate, String type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.gender = gender;
        this.birthDate = birthDate;
        this.type = type;
        this.search = type + bloodGroup;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
        this.search = this.type + bloodGroup;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.search = type + this.bloodGroup;
    }

    public String getSearch() {
        return search;
    }
} 