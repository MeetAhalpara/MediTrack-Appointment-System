package com.example.healthappointment.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Appointment {

    private final StringProperty patientId = new SimpleStringProperty();
    private final StringProperty patientName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty doctor = new SimpleStringProperty();
    private final StringProperty condition = new SimpleStringProperty();

    public Appointment(String patientId, String patientName, LocalDate date, String time, String doctor, String condition) {
        this.patientId.set(patientId);
        this.patientName.set(patientName);
        this.date.set(date);
        this.time.set(time);
        this.doctor.set(doctor);
        this.condition.set(condition);
    }

    // Properties
    public StringProperty patientIdProperty() { return patientId; }
    public StringProperty patientNameProperty() { return patientName; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty timeProperty() { return time; }
    public StringProperty doctorProperty() { return doctor; }
    public StringProperty conditionProperty() { return condition; }

    // Getters
    public String getPatientId() { return patientId.get(); }
    public String getPatientName() { return patientName.get(); }
    public LocalDate getDate() { return date.get(); }
    public String getTime() { return time.get(); }
    public String getDoctor() { return doctor.get(); }
    public String getCondition() { return condition.get(); }

    // Setters
    public void setPatientId(String id) { this.patientId.set(id); }
    public void setPatientName(String name) { this.patientName.set(name); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setTime(String time) { this.time.set(time); }
    public void setDoctor(String doctor) { this.doctor.set(doctor); }
    public void setCondition(String condition) { this.condition.set(condition); }
}