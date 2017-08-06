package com.zenpets.doctors.utils.models.doctors;

public class SpecializationsData {

    private String doctorSpecializationID;
    private String doctorID;
    private String doctorSpecializationName;

    public String getDoctorSpecializationID() {
        return doctorSpecializationID;
    }

    public void setDoctorSpecializationID(String doctorSpecializationID) {
        this.doctorSpecializationID = doctorSpecializationID;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public String getDoctorSpecializationName() {
        return doctorSpecializationName;
    }

    public void setDoctorSpecializationName(String doctorSpecializationName) {
        this.doctorSpecializationName = doctorSpecializationName;
    }
}