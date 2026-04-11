package com.example.healthappointment;

import com.example.healthappointment.util.InputValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InputValidatorTest — unit tests for the validation utility.
 *
 * Test plan reference: proposal Section 11 (Input validation testing).
 * Each test corresponds to a validation rule from Section 8.
 */
class InputValidatorTest {

    // -------------------------------------------------------------------------
    // Patient Name
    // -------------------------------------------------------------------------

    @Test
    void validateName_null_returnsError() {
        assertNotNull(InputValidator.validateName(null),
                "Null name should return an error message");
    }

    @Test
    void validateName_blank_returnsError() {
        assertNotNull(InputValidator.validateName("   "),
                "Blank name should return an error message");
    }

    @Test
    void validateName_valid_returnsNull() {
        assertNull(InputValidator.validateName("John Patel"),
                "Valid name should return null (no error)");
    }

    // -------------------------------------------------------------------------
    // Phone
    // -------------------------------------------------------------------------

    @Test
    void validatePhone_correctFormat_returnsNull() {
        assertNull(InputValidator.validatePhone("(613) 555-0182"),
                "Phone in (NNN) NNN-NNNN format should be accepted");
    }

    @Test
    void validatePhone_noSpacing_returnsError() {
        String result = InputValidator.validatePhone("6135550182");
        assertNotNull(result, "Phone without formatting should be rejected");
    }

    @Test
    void validatePhone_wrongSeparator_returnsError() {
        assertNotNull(InputValidator.validatePhone("613-555-0182"),
                "Phone with dashes instead of (NNN) format should be rejected");
    }

    @Test
    void validatePhone_empty_returnsError() {
        assertNotNull(InputValidator.validatePhone(""),
                "Empty phone should return an error message");
    }

    // -------------------------------------------------------------------------
    // Date
    // -------------------------------------------------------------------------

    @Test
    void validateDate_null_returnsError() {
        assertNotNull(InputValidator.validateDate(null),
                "Null date should return an error message");
    }

    @Test
    void validateDate_pastDate_returnsError() {
        assertNotNull(InputValidator.validateDate(LocalDate.now().minusDays(1)),
                "Yesterday's date should be rejected");
    }

    @Test
    void validateDate_today_returnsNull() {
        assertNull(InputValidator.validateDate(LocalDate.now()),
                "Today's date should be accepted");
    }

    @Test
    void validateDate_futureDate_returnsNull() {
        assertNull(InputValidator.validateDate(LocalDate.now().plusDays(7)),
                "Future date should be accepted");
    }

    // -------------------------------------------------------------------------
    // Time
    // -------------------------------------------------------------------------

    @Test
    void validateTime_null_returnsError() {
        assertNotNull(InputValidator.validateTime(null));
    }

    @Test
    void validateTime_blank_returnsError() {
        assertNotNull(InputValidator.validateTime(""));
    }

    @Test
    void validateTime_valid_returnsNull() {
        assertNull(InputValidator.validateTime("09:00 AM"));
    }

    // -------------------------------------------------------------------------
    // Doctor
    // -------------------------------------------------------------------------

    @Test
    void validateDoctor_blank_returnsError() {
        String result = InputValidator.validateDoctor("  ");
        assertNotNull(result, "Blank doctor name should return error");
    }

    @Test
    void validateDoctor_valid_returnsNull() {
        assertNull(InputValidator.validateDoctor("Dr. Smith"));
    }

    // -------------------------------------------------------------------------
    // Reason
    // -------------------------------------------------------------------------

    @Test
    void validateReason_blank_returnsError() {
        assertNotNull(InputValidator.validateReason(""));
    }

    @Test
    void validateReason_valid_returnsNull() {
        assertNull(InputValidator.validateReason("Flu"));
    }

    // -------------------------------------------------------------------------
    // validateAll — combined check
    // -------------------------------------------------------------------------

    @Test
    void validateAll_allValid_returnsEmpty() {
        String result = InputValidator.validateAll(
                "John Patel",
                "(613) 555-0182",
                LocalDate.now().plusDays(1),
                "09:00 AM",
                "Dr. Smith",
                "Flu");
        assertTrue(result.isEmpty(),
                "All valid inputs should produce an empty error string");
    }

    @Test
    void validateAll_multipleErrors_containsAllMessages() {
        String result = InputValidator.validateAll(
                "",                     // invalid name
                "0000000",              // invalid phone
                LocalDate.now().minusDays(3), // past date
                null,                   // no time
                "",                     // no doctor
                ""                      // no reason
        );

        assertFalse(result.isEmpty(), "Should collect multiple error messages");

        // 🔥 make checks case-insensitive (VERY IMPORTANT)
        String lower = result.toLowerCase();

        assertTrue(lower.contains("name"),  "Should mention name error");
        assertTrue(lower.contains("phone"), "Should mention phone error");
        assertTrue(lower.contains("date") || lower.contains("past"),
                "Should mention date/past error");
    }
}