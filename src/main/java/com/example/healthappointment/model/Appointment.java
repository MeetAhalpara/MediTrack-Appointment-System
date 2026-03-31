package com.example.healthappointment.model;

import java.time.LocalDate;

public class Appointment {

    private int appointmentId;
    private int patientId;
    private String patientName;
    private String phone;
    private LocalDate date;
    private String time;
    private String doctor;
    private String reason;
    private String status;

    public Appointment(int appointmentId, int patientId, String patientName,
                       String phone, LocalDate date, String time,
                       String doctor, String reason, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.doctor = doctor;
        this.reason = reason;
        this.status = status;
    }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int id) { this.appointmentId = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String name) { this.patientName = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}