package com.zenpets.doctors.utils.models.clinics.images;

import android.graphics.Bitmap;

public class ClinicAlbumCreatorData {

    private Bitmap bmpClinicImage;
    private String txtImageNumber;

    public Bitmap getBmpClinicImage() {
        return bmpClinicImage;
    }

    public void setBmpClinicImage(Bitmap bmpClinicImage) {
        this.bmpClinicImage = bmpClinicImage;
    }

    public String getTxtImageNumber() {
        return txtImageNumber;
    }

    public void setTxtImageNumber(String txtImageNumber) {
        this.txtImageNumber = txtImageNumber;
    }
}