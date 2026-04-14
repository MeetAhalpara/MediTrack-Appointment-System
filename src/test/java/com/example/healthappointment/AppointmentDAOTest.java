package com.example.healthappointment;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentDAOTest {

    private static final String TEST_DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/test_appointments.db";

    private static AppointmentDAO appointmentDAO;
    private static PatientDAO patientDAO;

    private int testPatientId;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        DatabaseConnection.setDatabaseUrl(TEST_DB_URL);
        DatabaseConnection.initializeDatabase();

        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
    }

    @BeforeEach
    void setUp() throws SQLException {
        clearDatabase();

        Patient patient = new Patient(0, "Test Patient", "(613) 000-0000");
        patientDAO.insert(patient);
        testPatientId = patient.getPatientId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearDatabase();
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        DatabaseConnection.closeConnection();

        File testDbFile = new File(System.getProperty("user.home"), "test_appointments.db");
        if (testDbFile.exists()) {
            testDbFile.delete();
        }

        DatabaseConnection.resetToDefaultDatabase();
    }

    private void clearDatabase() throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM appointments");
            stmt.execute("DELETE FROM patients");
        }
    }

    @Test
    @Order(1)
    void insert_generatesPositiveId() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(1),
                "09:00 AM",
                "Dr. Smith",
                "Flu",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);
        assertTrue(id > 0, "Generated appointment_id must be > 0");
    }

    @Test
    @Order(2)
    void findAll_returnsNonNullList() throws SQLException {
        List<Appointment> list = appointmentDAO.findAll();
        assertNotNull(list, "findAll() should never return null");
    }

    @Test
    @Order(3)
    void insert_thenFindAll_containsInsertedRecord() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "Dr. Lee",
                "Allergy",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);

        List<Appointment> all = appointmentDAO.findAll();
        boolean found = all.stream().anyMatch(a -> a.getAppointmentId() == id);

        assertTrue(found, "Inserted appointment should appear in findAll()");
    }

    @Test
    @Order(4)
    void findById_existingId_returnsPresent() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(3),
                "11:00 AM",
                "Dr. Johnson",
                "Headache",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);

        Optional<Appointment> found = appointmentDAO.findById(id);
        assertTrue(found.isPresent(), "findById should return a present Optional for a known ID");
        assertEquals("Dr. Johnson", found.get().getDoctor());
    }

    @Test
    @Order(5)
    void findById_nonExistentId_returnsEmpty() throws SQLException {
        Optional<Appointment> found = appointmentDAO.findById(Integer.MAX_VALUE);
        assertTrue(found.isEmpty(), "findById with a bogus ID should return empty Optional");
    }

    @Test
    @Order(6)
    void update_changesFieldsInDatabase() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(5),
                "01:00 PM",
                "Dr. Smith",
                "Flu",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);
        appt.setAppointmentId(id);
        appt.setDoctor("Dr. Patel");
        appt.setStatus("Completed");

        appointmentDAO.update(appt);

        Optional<Appointment> updated = appointmentDAO.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Dr. Patel", updated.get().getDoctor());
        assertEquals("Completed", updated.get().getStatus());
    }

    @Test
    @Order(7)
    void delete_removesAppointmentFromDatabase() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(6),
                "02:00 PM",
                "Dr. Williams",
                "Back Pain",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);
        appointmentDAO.delete(id);

        Optional<Appointment> deleted = appointmentDAO.findById(id);
        assertTrue(deleted.isEmpty(), "Deleted appointment should not be found");
    }

    @Test
    @Order(8)
    void searchByNameOrDoctor_matchesPartialName() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(7),
                "03:00 PM",
                "Dr. Smith",
                "Diabetes",
                "Scheduled"
        );

        appointmentDAO.insert(appt);

        List<Appointment> results = appointmentDAO.searchByNameOrDoctor("Test");
        assertFalse(results.isEmpty(),
                "searchByNameOrDoctor('Test') should return at least one result");
    }

    @Test
    @Order(9)
    void searchByNameOrDoctor_noMatch_returnsEmpty() throws SQLException {
        List<Appointment> results = appointmentDAO.searchByNameOrDoctor("ZZZNOMATCH999");
        assertTrue(results.isEmpty(),
                "Search for a non-existent string should return an empty list");
    }

    @Test
    @Order(10)
    void filterByStatus_scheduledOnly_returnsOnlyScheduled() throws SQLException {
        Appointment scheduled = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(8),
                "04:00 PM",
                "Dr. Brown",
                "Checkup",
                "Scheduled"
        );

        Appointment completed = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(9),
                "05:00 PM",
                "Dr. Green",
                "Follow-up",
                "Completed"
        );

        appointmentDAO.insert(scheduled);
        appointmentDAO.insert(completed);

        List<Appointment> results = appointmentDAO.filterByDateAndStatus(null, "Scheduled");

        assertFalse(results.isEmpty(), "Scheduled filter should return at least one record");
        for (Appointment a : results) {
            assertEquals("Scheduled", a.getStatus(),
                    "All filtered results should have status 'Scheduled'");
        }
    }

    @Test
    @Order(11)
    void deletePatient_cascadesAndRemovesAppointments() throws SQLException {
        Appointment appt = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(10),
                "06:00 PM",
                "Dr. Adams",
                "Consultation",
                "Scheduled"
        );

        int id = appointmentDAO.insert(appt);

        patientDAO.delete(testPatientId);

        Optional<Appointment> deleted = appointmentDAO.findById(id);
        assertTrue(deleted.isEmpty(),
                "Deleting the patient should cascade and remove related appointments");
    }
    @Test
    @Order(12)
    void filterByDate_onlyReturnsMatchingDate() throws SQLException {
        LocalDate targetDate = LocalDate.now().plusDays(12);

        Appointment matching = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                targetDate,
                "09:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        Appointment nonMatching = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                targetDate.plusDays(1),
                "10:00 AM",
                "Dr. Lee",
                "Follow-up",
                "Scheduled"
        );

        appointmentDAO.insert(matching);
        appointmentDAO.insert(nonMatching);

        List<Appointment> results = appointmentDAO.filterByDateAndStatus(targetDate, null);

        assertFalse(results.isEmpty(), "Date filter should return at least one record");
        for (Appointment a : results) {
            assertEquals(targetDate, a.getDate(),
                    "All returned appointments should match the selected date");
        }
    }

    @Test
    @Order(13)
    void filterByDateAndStatus_returnsExactMatches() throws SQLException {
        LocalDate targetDate = LocalDate.now().plusDays(15);

        Appointment expected = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                targetDate,
                "11:00 AM",
                "Dr. Adams",
                "Consultation",
                "Scheduled"
        );

        Appointment wrongStatus = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                targetDate,
                "12:00 PM",
                "Dr. Brown",
                "Review",
                "Completed"
        );

        Appointment wrongDate = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                targetDate.plusDays(1),
                "01:00 PM",
                "Dr. Clark",
                "Exam",
                "Scheduled"
        );

        appointmentDAO.insert(expected);
        appointmentDAO.insert(wrongStatus);
        appointmentDAO.insert(wrongDate);

        List<Appointment> results = appointmentDAO.filterByDateAndStatus(targetDate, "Scheduled");

        assertFalse(results.isEmpty(), "Combined filter should return at least one matching record");
        for (Appointment a : results) {
            assertEquals(targetDate, a.getDate(),
                    "All returned appointments should match the selected date");
            assertEquals("Scheduled", a.getStatus(),
                    "All returned appointments should match the selected status");
        }

        boolean foundExpected = results.stream().anyMatch(a ->
                a.getDoctor().equals("Dr. Adams") &&
                        a.getDate().equals(targetDate) &&
                        a.getStatus().equals("Scheduled")
        );

        assertTrue(foundExpected, "The exact matching appointment should be included in results");
    }

    @Test
    @Order(14)
    void delete_nonExistingId_doesNotCrash() {
        assertDoesNotThrow(() -> appointmentDAO.delete(Integer.MAX_VALUE),
                "Deleting a non-existing appointment ID should not crash");
    }

    @Test
    @Order(15)
    void update_nonExistingId_doesNotCorruptData() throws SQLException {
        Appointment real = new Appointment(
                0,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(20),
                "02:00 PM",
                "Dr. Real",
                "Checkup",
                "Scheduled"
        );

        int realId = appointmentDAO.insert(real);

        Appointment fake = new Appointment(
                Integer.MAX_VALUE,
                testPatientId,
                "Test Patient",
                "(613) 000-0000",
                LocalDate.now().plusDays(21),
                "03:00 PM",
                "Dr. Fake",
                "Fake Update",
                "Completed"
        );

        assertDoesNotThrow(() -> appointmentDAO.update(fake),
                "Updating a non-existing appointment should not crash");

        Optional<Appointment> unchanged = appointmentDAO.findById(realId);
        assertTrue(unchanged.isPresent(), "The real appointment should still exist");
        assertEquals("Dr. Real", unchanged.get().getDoctor(),
                "Updating a fake record must not corrupt existing data");
        assertEquals("Scheduled", unchanged.get().getStatus(),
                "Existing appointment status should remain unchanged");
    }
}