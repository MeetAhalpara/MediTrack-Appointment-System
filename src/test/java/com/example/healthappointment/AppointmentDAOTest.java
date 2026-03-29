package com.example.healthappointment;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AppointmentDAOTest — integration tests for the DAO / database layer.
 *
 * Test plan reference: proposal Section 11 (CRUD functionality testing,
 * Database testing, Search and filter testing).
 *
 * Each test uses the DatabaseConnection singleton which points to the
 * SQLite file on disk (or an in-memory DB if the URL is reconfigured).
 * setUp() seeds a fresh patient; tearDown() removes all test data.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentDAOTest {

    private static AppointmentDAO appointmentDAO;
    private static PatientDAO     patientDAO;

    private static int testPatientId;
    private static int insertedApptId;

    @BeforeAll
    static void setupDAO() {
        appointmentDAO = new AppointmentDAO();
        patientDAO     = new PatientDAO();
    }

    @BeforeEach
    void seedPatient() throws SQLException {
        // Insert a test patient for each test (in case previous test cleaned up)
        Patient p = new Patient(0, "Test Patient", "(613) 000-0000");
        patientDAO.insert(p);
        testPatientId = p.getPatientId();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Delete test patient — CASCADE removes their appointments too
        patientDAO.delete(testPatientId);
    }

    // -------------------------------------------------------------------------
    // Insert (UC1 — Create Appointment)
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void insert_generatesPositiveId() throws SQLException {
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(1), "09:00 AM", "Dr. Smith", "Flu", "Scheduled");
        int id = appointmentDAO.insert(appt);
        assertTrue(id > 0, "Generated appointment_id must be > 0");
        insertedApptId = id;
    }

    // -------------------------------------------------------------------------
    // FindAll (UC2 — View Appointments)
    // -------------------------------------------------------------------------

    @Test
    @Order(2)
    void findAll_returnsNonNullList() throws SQLException {
        List<Appointment> list = appointmentDAO.findAll();
        assertNotNull(list, "findAll() should never return null");
    }

    @Test
    @Order(3)
    void insert_thenFindAll_containsInsertedRecord() throws SQLException {
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(2), "10:00 AM", "Dr. Lee", "Allergy", "Scheduled");
        int id = appointmentDAO.insert(appt);

        List<Appointment> all = appointmentDAO.findAll();
        boolean found = all.stream().anyMatch(a -> a.getAppointmentId() == id);
        assertTrue(found, "Inserted appointment should appear in findAll()");
    }

    // -------------------------------------------------------------------------
    // FindById
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    void findById_existingId_returnsPresent() throws SQLException {
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(3), "11:00 AM", "Dr. Johnson", "Headache", "Scheduled");
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

    // -------------------------------------------------------------------------
    // Update (UC3)
    // -------------------------------------------------------------------------

    @Test
    @Order(6)
    void update_changesFieldsInDatabase() throws SQLException {
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(5), "01:00 PM", "Dr. Smith", "Flu", "Scheduled");
        int id = appointmentDAO.insert(appt);

        appt.setDoctor("Dr. Patel");
        appt.setStatus("Completed");
        appointmentDAO.update(appt);

        Optional<Appointment> updated = appointmentDAO.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Dr. Patel",  updated.get().getDoctor());
        assertEquals("Completed",  updated.get().getStatus());
    }

    // -------------------------------------------------------------------------
    // Delete (UC4)
    // -------------------------------------------------------------------------

    @Test
    @Order(7)
    void delete_removesAppointmentFromDatabase() throws SQLException {
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(6), "02:00 PM", "Dr. Williams", "Back Pain", "Scheduled");
        int id = appointmentDAO.insert(appt);

        appointmentDAO.delete(id);

        Optional<Appointment> deleted = appointmentDAO.findById(id);
        assertTrue(deleted.isEmpty(), "Deleted appointment should not be found");
    }

    // -------------------------------------------------------------------------
    // Search (UC5 — Search / Filter)
    // -------------------------------------------------------------------------

    @Test
    @Order(8)
    void searchByNameOrDoctor_matchesPartialName() throws SQLException {
        // Insert appointment and search for part of the patient name
        Appointment appt = new Appointment(0, testPatientId, "Test Patient", "(613) 000-0000",
                LocalDate.now().plusDays(7), "03:00 PM", "Dr. Smith", "Diabetes", "Scheduled");
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

    // -------------------------------------------------------------------------
    // Filter (UC5)
    // -------------------------------------------------------------------------

    @Test
    @Order(10)
    void filterByStatus_scheduledOnly_returnsOnlyScheduled() throws SQLException {
        List<Appointment> results = appointmentDAO.filterByDateAndStatus(null, "Scheduled");
        for (Appointment a : results) {
            assertEquals("Scheduled", a.getStatus(),
                    "All filtered results should have status 'Scheduled'");
        }
    }
}