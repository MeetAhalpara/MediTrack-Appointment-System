package com.example.healthappointment;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.DatabaseConnection;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public abstract class BaseUiTest extends ApplicationTest {

    protected static final String TEST_DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/test_health_appointment_ui.db";

    protected final AppointmentDAO appointmentDAO = new AppointmentDAO();
    protected final PatientDAO patientDAO = new PatientDAO();

    @BeforeAll
    static void setUpTestDatabase() throws Exception {
        deleteTestDatabaseFile();
        DatabaseConnection.setDatabaseUrl(TEST_DB_URL);
        DatabaseConnection.initializeDatabase();
    }

    @AfterAll
    static void tearDownTestDatabase() throws Exception {
        DatabaseConnection.closeConnection();
        deleteTestDatabaseFile();
        DatabaseConnection.resetToDefaultDatabase();
    }

    @Start
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
        stage.toFront();
    }

    @BeforeEach
    void resetState() throws Exception {
        clearDatabase();
        refreshMainTable();
    }

    protected void clearDatabase() throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM appointments");
            stmt.execute("DELETE FROM patients");
        }
    }

    protected void openSidebarIfHidden(ApplicationTest robot) {
        VBox sidebar = robot.lookup("#sidebar").queryAs(VBox.class);

        if (!sidebar.isVisible()) {
            robot.clickOn("#hamburgerButton");
            robot.sleep(700);
        }
    }

    protected void refreshMainTable() {
        if (lookup("#clearFilterButton").tryQuery().isPresent()) {
            clickOn("#clearFilterButton");
            sleep(400);
        } else {
            sleep(400);
        }
    }

    protected Appointment seedAppointment(
            String patientName,
            String phone,
            LocalDate date,
            String time,
            String doctor,
            String reason,
            String status
    ) throws SQLException {

        Patient patient = new Patient(0, patientName, phone);
        patientDAO.insert(patient);

        Appointment appointment = new Appointment(
                0,
                patient.getPatientId(),
                patientName,
                phone,
                date,
                time,
                doctor,
                reason,
                status
        );

        int id = appointmentDAO.insert(appointment);
        appointment.setAppointmentId(id);
        return appointment;
    }

    protected Appointment seedDefaultAppointment() throws SQLException {
        Appointment appointment = seedAppointment(
                "John Carter",
                "(613) 555-1234",
                LocalDate.now().plusDays(3),
                "10:00 AM",
                "Dr. Smith",
                "General Checkup",
                "Scheduled"
        );
        refreshMainTable();
        return appointment;
    }

    protected static void deleteTestDatabaseFile() {
        File file = new File(System.getProperty("user.home"), "test_health_appointment_ui.db");
        if (file.exists()) {
            file.delete();
        }
    }
}