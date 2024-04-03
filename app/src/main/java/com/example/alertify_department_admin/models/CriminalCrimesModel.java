package com.example.alertify_department_admin.models;

public class CriminalCrimesModel {
    private String FIR;
    private String PoliceStation;
    private String District;

    public String getFIR() {
        return FIR;
    }

    public void setFIR(String FIR) {
        this.FIR = FIR;
    }

    public String getPoliceStation() {
        return PoliceStation;
    }

    public void setPoliceStation(String policeStation) {
        PoliceStation = policeStation;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        District = district;
    }
}
