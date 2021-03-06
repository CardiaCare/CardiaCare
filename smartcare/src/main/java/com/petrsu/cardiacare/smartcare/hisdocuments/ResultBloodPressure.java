package com.petrsu.cardiacare.smartcare.hisdocuments;

import java.io.Serializable;

public class ResultBloodPressure implements Serializable {

    String systolicPressure;
    String diastolicPressure;
    int id;
    String pulse;
    String time;

    public ResultBloodPressure(String systolicPressure, String diastolicPressure, String pulse,String time, int id) {
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.pulse = pulse;
        this.time = time;
        this.id = id;
    }

    public int getIdPressure() { return id; }
    public void setIdPressure(int id) {
        this.id = id;
    }

    public String getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(String systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public String getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(String diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
