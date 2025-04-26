package com.example.bloodbank.Model;

public class User {
    String name, idnumber, gender, email, address, birthdate, bloodgroup, id, phonenumber,
            profilepictureurl, search, type;

    public User() {
    }

    public User(String name, String idnumber, String gender, String email, String address, String birthdate, String bloodgroup, String id, String phonenumber, String profilepictureurl, String search, String type) {
        this.name = name;
        this.idnumber = idnumber;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.birthdate = birthdate;
        this.bloodgroup = bloodgroup;
        this.id = id;
        this.phonenumber = phonenumber;
        this.profilepictureurl = profilepictureurl;
        this.search = search;
        this.type = type;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdnumber() {
        return idnumber;
    }

    public void setIdnumber(String idnumber) {
        this.idnumber = idnumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getProfilepictureurl() {
        return profilepictureurl;
    }

    public void setProfilepictureurl(String profilepictureurl) {
        this.profilepictureurl = profilepictureurl;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
