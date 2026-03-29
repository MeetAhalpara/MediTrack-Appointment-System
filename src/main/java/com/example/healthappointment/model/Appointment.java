package com.example.healthappointment.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Model class representing an Appointment.
 * Updated to include a status field and a foreign-key reference to Patient,
 * matching the normalized database design in Section 6 of the project proposal.
 */
public class Appointment {

    private final IntegerProperty            appointmentId = new SimpleIntegerProperty();
    private final IntegerProperty            patientId     = new SimpleIntegerProperty();
    private final StringProperty             patientName   = new SimpleStringProperty();
    private final StringProperty             phone         = new SimpleStringProperty();
    private final ObjectProperty<LocalDate>  date          = new SimpleObjectProperty<>();
    private final StringProperty             time          = new SimpleStringProperty();
    private final StringProperty             doctor        = new SimpleStringProperty();
    private final StringProperty             reason        = new SimpleStringProperty();
    private final StringProperty             status        = new SimpleStringProperty();

    public Appointment() {}

    public Appointment(int appointmentId, int patientId, String patientName, String phone,
                       LocalDate date, String time, String doctor, String reason, String status) {
        this.appointmentId.set(appointmentId);
        this.patientId.set(patientId);
        this.patientName.set(patientName);
        this.phone.set(phone);
        this.date.set(date);
        this.time.set(time);
        this.doctor.set(doctor);
        this.reason.set(reason);
        this.status.set(status);
    }

    // --- Properties ---
    public IntegerProperty           appointmentIdProperty() { return appointmentId; }
    public IntegerProperty           patientIdProperty()     { return patientId; }
    public StringProperty            patientNameProperty()   { return patientName; }
    public StringProperty            phoneProperty()         { return phone; }
    public ObjectProperty<LocalDate> dateProperty()          { return date; }
    public StringProperty            timeProperty()          { return time; }
    public StringProperty            doctorProperty()        { return doctor; }
    public StringProperty            reasonProperty()        { return reason; }
    public StringProperty            statusProperty()        { return status; }

    // --- Getters ---
    public int       getAppointmentId() { return appointmentId.get(); }
    public int       getPatientId()     { return patientId.get(); }
    public String    getPatientName()   { return patientName.get(); }
    public String    getPhone()         { return phone.get(); }
    public LocalDate getDate()          { return date.get(); }
    public String    getTime()          { return time.get(); }
    public String    getDoctor()        { return doctor.get(); }
    public String    getReason()        { return reason.get(); }
    public String    getStatus()        { return status.get(); }

    // --- Setters ---
    public void setAppointmentId(int id)      { this.appointmentId.set(id); }
    public void setPatientId(int id)          { this.patientId.set(id); }
    public void setPatientName(String name)   { this.patientName.set(name); }
    public void setPhone(String phone)        { this.phone.set(phone); }
    public void setDate(LocalDate date)       { this.date.set(date); }
    public void setTime(String time)          { this.time.set(time); }
    public void setDoctor(String doctor)      { this.doctor.set(doctor); }
    public void setReason(String reason)      { this.reason.set(reason); }
    public void setStatus(String status)      { this.status.set(status); }
}