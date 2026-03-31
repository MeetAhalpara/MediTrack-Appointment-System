package com.example.healthappointment.util;

import java.time.LocalDate;

public class InputValidator {

    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty())
            return "Name is required";
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return "Phone is required";
        if (!phone.matches("\\d{10}|\\(\\d{3}\\) \\d{3}-\\d{4}"))
            return "Invalid phone format";
        return null;
    }

    public static String validateDate(LocalDate date) {
        if (date == null)
            return "Date is required";
        if (date.isBefore(LocalDate.now()))
            return "Date cannot be in the past";
        return null;
    }

    public static String validateTime(String time) {
        if (time == null || time.isEmpty())
            return "Select a time";
        return null;
    }

    public static String validateDoctor(String doctor) {
        if (doctor == null || doctor.isEmpty())
            return "Select a doctor";
        return null;
    }

    public static String validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty())
            return "Reason required";
        return null;
    }

    public static String validateAll(String name, String phone, LocalDate date,
                                     String time, String doctor, String reason) {

        StringBuilder sb = new StringBuilder();

        append(sb, validateName(name));
        append(sb, validatePhone(phone));
        append(sb, validateDate(date));
        append(sb, validateTime(time));
        append(sb, validateDoctor(doctor));
        append(sb, validateReason(reason));

        return sb.toString();
    }

    private static void append(StringBuilder sb, String error) {
        if (error != null) sb.append("• ").append(error).append("\n");
    }
}