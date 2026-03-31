package com.example.healthappointment.model;

public class Patient {

    private int patientId;
    private String name;
    private String phone;

    public Patient(int patientId, String name, String phone) {
        this.patientId = patientId;
        this.name = name;
        this.phone = phone;
    }

    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}