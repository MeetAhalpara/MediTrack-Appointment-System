package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.InputValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * AppointmentFormController — handles both Add and Edit workflows.
 *
 * Proposal references:
 *  Section 2  — Core feature: Add / Edit appointment
 *  Section 8  — Input validation strategy
 *  Section 3  — Screen 2: Add / Edit Appointment Form
 *  Section 7  — Accessibility: logical Tab order (Patient Name → Phone →
 *               Date → Time → Doctor → Reason → Status → Save)
 */
public class AppointmentFormController {

    // ---- Form fields --------------------------------------------------------
    @FXML private TextField         fieldName;
    @FXML private TextField         fieldPhone;
    @FXML private DatePicker        fieldDate;
    @FXML private ComboBox<String>  fieldTime;
    @FXML private ComboBox<String>  fieldDoctor;
    @FXML private TextField         fieldReason;
    @FXML private ComboBox<String>  fieldStatus;

    // ---- Inline validation labels (proposal Section 8) ----------------------
    @FXML private Label lblErrName;
    @FXML private Label lblErrPhone;
    @FXML private Label lblErrDate;
    @FXML private Label lblErrTime;
    @FXML private Label lblErrDoctor;
    @FXML private Label lblErrReason;

    // ---- Title label to distinguish Add vs Edit mode -------------------------
    @FXML private Label lblTitle;

    // ---- State ---------------------------------------------------------------
    private Appointment      editTarget;   // null = Add mode; non-null = Edit mode
    private MainController   mainController;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO     patientDAO     = new PatientDAO();

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        fieldTime.setItems(FXCollections.observableArrayList(
                "09:00 AM", "10:00 AM", "11:00 AM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"));

        fieldDoctor.setItems(FXCollections.observableArrayList(
                "Dr. Smith", "Dr. Johnson", "Dr. Lee",
                "Dr. Patel", "Dr. Williams"));

        fieldStatus.setItems(FXCollections.observableArrayList(
                "Scheduled", "Completed", "Cancelled"));
        fieldStatus.setValue("Scheduled");

        // Clear inline error labels initially
        clearErrors();

        // Real-time phone formatting hint (proposal Section 8)
        fieldPhone.setPromptText("(613) 555-0182");

        // Inline validation on focus-lost (proposal Section 8)
        fieldName.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> { if (!isFocused) validateNameInline(); });
        fieldPhone.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> { if (!isFocused) validatePhoneInline(); });
        fieldDate.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> { if (!isFocused) validateDateInline(); });
    }

    // -------------------------------------------------------------------------
    // Public API — called by MainController
    // -------------------------------------------------------------------------

    /** Pass null for Add mode; pass an existing Appointment for Edit mode. */
    public void setAppointment(Appointment appt) {
        this.editTarget = appt;
        if (appt != null) {
            lblTitle.setText("Edit Appointment");
            fieldName.setText(appt.getPatientName());
            fieldPhone.setText(appt.getPhone());
            fieldDate.setValue(appt.getDate());
            fieldTime.setValue(appt.getTime());
            fieldDoctor.setValue(appt.getDoctor());
            fieldReason.setText(appt.getReason());
            fieldStatus.setValue(appt.getStatus());
        } else {
            lblTitle.setText("Add Appointment");
        }
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    // -------------------------------------------------------------------------
    // Save handler
    // -------------------------------------------------------------------------

    @FXML
    private void handleSave() {
        String name   = fieldName.getText().trim();
        String phone  = fieldPhone.getText().trim();
        LocalDate date = fieldDate.getValue();
        String time   = fieldTime.getValue();
        String doctor = fieldDoctor.getValue();
        String reason = fieldReason.getText().trim();
        String status = fieldStatus.getValue();

        // --- Full validation (proposal Section 8) ---
        String errors = InputValidator.validateAll(name, phone, date, time, doctor, reason);
        if (!errors.isEmpty()) {
            // Show inline errors
            showInlineErrors(name, phone, date, time, doctor, reason);
            // Also show summary alert for accessibility
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Validation Error");
            a.setHeaderText("Please correct the following:");
            a.setContentText(errors);
            a.showAndWait();
            return;
        }

        try {
            if (editTarget == null) {
                // ---- ADD mode ----
                // Insert or find patient (normalised schema — proposal Section 6)
                Patient patient = new Patient(0, name, phone);
                patientDAO.insert(patient);

                Appointment appt = new Appointment(
                        0, patient.getPatientId(), name, phone,
                        date, time, doctor, reason,
                        status != null ? status : "Scheduled");
                appointmentDAO.insert(appt);
            } else {
                // ---- EDIT mode ----
                // Update patient record first
                Patient patient = new Patient(
                        editTarget.getPatientId(), name, phone);
                patientDAO.update(patient);

                // Update appointment
                editTarget.setPatientName(name);
                editTarget.setPhone(phone);
                editTarget.setDate(date);
                editTarget.setTime(time);
                editTarget.setDoctor(doctor);
                editTarget.setReason(reason);
                editTarget.setStatus(status);
                appointmentDAO.update(editTarget);
            }

            // Refresh dashboard
            if (mainController != null) mainController.loadAppointmentsFromDB();
            closeWindow();

        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Database Error");
            a.setHeaderText("Could not save appointment");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // -------------------------------------------------------------------------
    // Inline validation helpers (proposal Section 8)
    // -------------------------------------------------------------------------

    private void validateNameInline() {
        String err = InputValidator.validateName(fieldName.getText());
        lblErrName.setText(err != null ? err : "");
    }

    private void validatePhoneInline() {
        String err = InputValidator.validatePhone(fieldPhone.getText());
        lblErrPhone.setText(err != null ? err : "");
    }

    private void validateDateInline() {
        String err = InputValidator.validateDate(fieldDate.getValue());
        lblErrDate.setText(err != null ? err : "");
    }

    private void showInlineErrors(String name, String phone, LocalDate date,
                                  String time, String doctor, String reason) {
        lblErrName.setText(orEmpty(InputValidator.validateName(name)));
        lblErrPhone.setText(orEmpty(InputValidator.validatePhone(phone)));
        lblErrDate.setText(orEmpty(InputValidator.validateDate(date)));
        lblErrTime.setText(orEmpty(InputValidator.validateTime(time)));
        lblErrDoctor.setText(orEmpty(InputValidator.validateDoctor(doctor)));
        lblErrReason.setText(orEmpty(InputValidator.validateReason(reason)));
    }

    private void clearErrors() {
        lblErrName.setText("");
        lblErrPhone.setText("");
        lblErrDate.setText("");
        lblErrTime.setText("");
        lblErrDoctor.setText("");
        lblErrReason.setText("");
    }

    private String orEmpty(String s) { return s != null ? s : ""; }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    private void closeWindow() {
        ((Stage) fieldName.getScene().getWindow()).close();
    }
}