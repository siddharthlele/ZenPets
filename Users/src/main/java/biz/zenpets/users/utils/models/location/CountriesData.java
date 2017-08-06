package biz.zenpets.users.utils.models.location;

import android.graphics.drawable.Drawable;

public class CountriesData {

    private String countryID;
    private String countryName;
    private String currencyName;
    private String currencyCode;
    private String currencySymbol;
    private Drawable countryFlag;

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public Drawable getCountryFlag() {
        return countryFlag;
    }

    public void setCountryFlag(Drawable countryFlag) {
        this.countryFlag = countryFlag;
    }
}