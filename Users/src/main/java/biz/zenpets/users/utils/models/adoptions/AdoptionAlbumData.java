package biz.zenpets.users.utils.models.adoptions;

import android.graphics.Bitmap;

public class AdoptionAlbumData {

    private Bitmap bmpAdoptionImage;
    private String txtImageNumber;

    public Bitmap getBmpAdoptionImage() {
        return bmpAdoptionImage;
    }

    public void setBmpAdoptionImage(Bitmap bmpAdoptionImage) {
        this.bmpAdoptionImage = bmpAdoptionImage;
    }

    public String getTxtImageNumber() {
        return txtImageNumber;
    }

    public void setTxtImageNumber(String txtImageNumber) {
        this.txtImageNumber = txtImageNumber;
    }
}