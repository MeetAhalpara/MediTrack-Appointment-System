package com.example.healthappointment.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * InputValidator — centralised validation logic.
 *
 * Implements the rules defined in proposal Section 8:
 *  • Patient name cannot be empty
 *  • Phone must match (613) 555-0182 format → general: (NNN) NNN-NNNN
 *  • Date cannot be in the past
 *  • Time must not be null
 *  • Doctor name cannot be empty
 *  • Reason cannot be empty
 */
public class InputValidator {

    /** Regex: (NNN) NNN-NNNN  e.g.  (613) 555-0182 */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\(\\d{3}\\) \\d{3}-\\d{4}$");

    private InputValidator() {}   // utility class — no instantiation

    /** Returns null if valid; otherwise a human-readable error message. */
    public static String validateName(String name) {
        if (name == null || name.isBlank())
            return "Patient name cannot be empty.";
        return null;
    }

    /**
     * Validates phone in (NNN) NNN-NNNN format.
     * Returns null if valid; an error message otherwise.
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank())
            return "Phone number cannot be empty.";
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return "Phone must be in the format (613) 555-0182.";
        return null;
    }

    /**
     * Validates that the date is today or in the future.
     * Returns null if valid; an error message otherwise.
     */
    public static String validateDate(LocalDate date) {
        if (date == null)
            return "Date is required.";
        if (date.isBefore(LocalDate.now()))
            return "Date cannot be in the past.";
        return null;
    }

    /** Returns null if valid; otherwise a human-readable error message. */
    public static String validateTime(String time) {
        if (time == null || time.isBlank())
            return "Time is required.";
        return null;
    }

    /** Returns null if valid; otherwise a human-readable error message. */
    public static String validateDoctor(String doctor) {
        if (doctor == null || doctor.isBlank())
            return "Doctor name cannot be empty.";
        return null;
    }

    /** Returns null if valid; otherwise a human-readable error message. */
    public static String validateReason(String reason) {
        if (reason == null || reason.isBlank())
            return "Reason / condition is required.";
        return null;
    }

    /**
     * Convenience method — validates all appointment form fields at once.
     *
     * @return an empty string if everything is valid; otherwise a newline-separated
     *         list of error messages ready to show in an Alert.
     */
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

    private static void append(StringBuilder sb, String msg) {
        if (msg != null) sb.append("• ").append(msg).append("\n");
    }
}