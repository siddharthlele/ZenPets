package biz.zenpets.users.utils.models.pet;

public class VaccinationsData {

    private String vaccinationID;
    private String petID;
    private String vaccineID;
    private String vaccineName;
    private String vaccinationDate;
    private String vaccinationNotes;
    private String vaccinationPicture;

    public String getVaccinationID() {
        return vaccinationID;
    }

    public void setVaccinationID(String vaccinationID) {
        this.vaccinationID = vaccinationID;
    }

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
    }

    public String getVaccineID() {
        return vaccineID;
    }

    public void setVaccineID(String vaccineID) {
        this.vaccineID = vaccineID;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getVaccinationDate() {
        return vaccinationDate;
    }

    public void setVaccinationDate(String vaccinationDate) {
        this.vaccinationDate = vaccinationDate;
    }

    public String getVaccinationNotes() {
        return vaccinationNotes;
    }

    public void setVaccinationNotes(String vaccinationNotes) {
        this.vaccinationNotes = vaccinationNotes;
    }

    public String getVaccinationPicture() {
        return vaccinationPicture;
    }

    public void setVaccinationPicture(String vaccinationPicture) {
        this.vaccinationPicture = vaccinationPicture;
    }
}