package biz.zenpets.users.utils.models.adoptions;

import java.util.ArrayList;

public class AdoptionsData {

    private String adoptionID;
    private String petTypeID;
    private String petTypeName;
    private String breedID;
    private String breedName;
    private String userID;
    private String userName;
    private String cityID;
    private String cityName;
    private String adoptionName;
    private String adoptionDescription;
    private String adoptionGender;
    private String adoptionVaccination;
    private String adoptionDewormed;
    private String adoptionNeutered;
    private String adoptionTimeStamp;
    private String adoptionStatus;
    private ArrayList<AdoptionsImageData> images;

    public String getAdoptionID() {
        return adoptionID;
    }

    public void setAdoptionID(String adoptionID) {
        this.adoptionID = adoptionID;
    }

    public String getPetTypeID() {
        return petTypeID;
    }

    public void setPetTypeID(String petTypeID) {
        this.petTypeID = petTypeID;
    }

    public String getPetTypeName() {
        return petTypeName;
    }

    public void setPetTypeName(String petTypeName) {
        this.petTypeName = petTypeName;
    }

    public String getBreedID() {
        return breedID;
    }

    public void setBreedID(String breedID) {
        this.breedID = breedID;
    }

    public String getBreedName() {
        return breedName;
    }

    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAdoptionName() {
        return adoptionName;
    }

    public void setAdoptionName(String adoptionName) {
        this.adoptionName = adoptionName;
    }

    public String getAdoptionDescription() {
        return adoptionDescription;
    }

    public void setAdoptionDescription(String adoptionDescription) {
        this.adoptionDescription = adoptionDescription;
    }

    public String getAdoptionGender() {
        return adoptionGender;
    }

    public void setAdoptionGender(String adoptionGender) {
        this.adoptionGender = adoptionGender;
    }

    public String getAdoptionVaccination() {
        return adoptionVaccination;
    }

    public void setAdoptionVaccination(String adoptionVaccination) {
        this.adoptionVaccination = adoptionVaccination;
    }

    public String getAdoptionDewormed() {
        return adoptionDewormed;
    }

    public void setAdoptionDewormed(String adoptionDewormed) {
        this.adoptionDewormed = adoptionDewormed;
    }

    public String getAdoptionNeutered() {
        return adoptionNeutered;
    }

    public void setAdoptionNeutered(String adoptionNeutered) {
        this.adoptionNeutered = adoptionNeutered;
    }

    public String getAdoptionTimeStamp() {
        return adoptionTimeStamp;
    }

    public void setAdoptionTimeStamp(String adoptionTimeStamp) {
        this.adoptionTimeStamp = adoptionTimeStamp;
    }

    public String getAdoptionStatus() {
        return adoptionStatus;
    }

    public void setAdoptionStatus(String adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }

    public ArrayList<AdoptionsImageData> getImages() {
        return images;
    }

    public void setImages(ArrayList<AdoptionsImageData> images) {
        this.images = images;
    }
}