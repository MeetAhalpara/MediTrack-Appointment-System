package com.example.healthappointment.model;

import javafx.beans.property.*;

/**
 * Model class representing a Patient.
 * Normalized from the original single-table design per the improved database design
 * described in the project proposal (Section 6).
 */
public class Patient {

    private final IntegerProperty patientId = new SimpleIntegerProperty();
    private final StringProperty name        = new SimpleStringProperty();
    private final StringProperty phone       = new SimpleStringProperty();

    public Patient() {}

    public Patient(int patientId, String name, String phone) {
        this.patientId.set(patientId);
        this.name.set(name);
        this.phone.set(phone);
    }

    // --- Properties ---
    public IntegerProperty patientIdProperty() { return patientId; }
    public StringProperty  nameProperty()      { return name; }
    public StringProperty  phoneProperty()     { return phone; }

    // --- Getters ---
    public int    getPatientId() { return patientId.get(); }
    public String getName()     { return name.get(); }
    public String getPhone()    { return phone.get(); }

    // --- Setters ---
    public void setPatientId(int id)     { this.patientId.set(id); }
    public void setName(String name)     { this.name.set(name); }
    public void setPhone(String phone)   { this.phone.set(phone); }

    @Override
    public String toString() {
        return getName(); // used by ComboBox display
    }
}