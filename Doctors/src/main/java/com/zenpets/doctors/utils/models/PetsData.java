package com.zenpets.doctors.utils.models;

public class PetsData {

    private String petID;
    private String userID;
    private String petName;
    private String petGender;
    private String petDOB;
    private String petProfile;

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetGender() {
        return petGender;
    }

    public void setPetGender(String petGender) {
        this.petGender = petGender;
    }

    public String getPetDOB() {
        return petDOB;
    }

    public void setPetDOB(String petDOB) {
        this.petDOB = petDOB;
    }

    public String getPetProfile() {
        return petProfile;
    }

    public void setPetProfile(String petProfile) {
        this.petProfile = petProfile;
    }
}