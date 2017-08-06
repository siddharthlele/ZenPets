package com.zenpets.doctors.utils.models.clinics.images;

import android.graphics.Bitmap;

public class ClinicAlbumData {

    private String imageID = null;
    private String clinicID = null;
    private String imageURL = null;

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getClinicID() {
        return clinicID;
    }

    public void setClinicID(String clinicID) {
        this.clinicID = clinicID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}