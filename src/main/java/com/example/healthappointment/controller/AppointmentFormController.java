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

public class AppointmentFormController {

    // ---- Form fields --------------------------------------------------------
    @FXML private TextField fieldName;
    @FXML private TextField fieldPhone;
    @FXML private DatePicker fieldDate;
    @FXML private ComboBox<String> fieldTime;
    @FXML private ComboBox<String> fieldDoctor;
    @FXML private TextField fieldReason;
    @FXML private ComboBox<String> fieldStatus;

    // ---- Error labels -------------------------------------------------------
    @FXML private Label lblErrName;
    @FXML private Label lblErrPhone;
    @FXML private Label lblErrDate;
    @FXML private Label lblErrTime;
    @FXML private Label lblErrDoctor;
    @FXML private Label lblErrReason;

    @FXML private Label lblTitle;

    // ---- State --------------------------------------------------------------
    private Appointment editTarget;
    private MainController mainController;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    // -------------------------------------------------------------------------
    // INITIALIZE
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

        clearErrors();

        // 🔥 Auto phone formatter
        applyPhoneFormatter();

        // Inline validation
        fieldName.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validateNameInline(); });

        fieldPhone.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validatePhoneInline(); });

        fieldDate.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validateDateInline(); });
    }

    // -------------------------------------------------------------------------
    // 🔥 PHONE FORMATTER (MAIN FEATURE)
    // -------------------------------------------------------------------------
    private void applyPhoneFormatter() {

        fieldPhone.textProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue == null) return;

            // Remove non-digits
            String digits = newValue.replaceAll("\\D", "");

            // Limit to 10 digits
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            StringBuilder formatted = new StringBuilder();

            if (digits.length() >= 1) {
                formatted.append("(");
            }

            if (digits.length() >= 3) {
                formatted.append(digits.substring(0, 3)).append(") ");
            } else {
                formatted.append(digits);
            }

            if (digits.length() >= 6) {
                formatted.append(digits.substring(3, 6)).append("-");
                formatted.append(digits.substring(6));
            } else if (digits.length() > 3) {
                formatted.append(digits.substring(3));
            }

            // Prevent loop
            if (!formatted.toString().equals(newValue)) {
                fieldPhone.setText(formatted.toString());
                fieldPhone.positionCaret(formatted.length());
            }
        });
    }

    // -------------------------------------------------------------------------
    // SET DATA
    // -------------------------------------------------------------------------
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
    // SAVE
    // -------------------------------------------------------------------------
    @FXML
    private void handleSave() {

        String name = fieldName.getText().trim();
        String phone = fieldPhone.getText().trim();
        LocalDate date = fieldDate.getValue();
        String time = fieldTime.getValue();
        String doctor = fieldDoctor.getValue();
        String reason = fieldReason.getText().trim();
        String status = fieldStatus.getValue();

        String errors = InputValidator.validateAll(name, phone, date, time, doctor, reason);

        if (!errors.isEmpty()) {
            showInlineErrors(name, phone, date, time, doctor, reason);

            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Validation Error");
            a.setHeaderText("Fix the following:");
            a.setContentText(errors);
            a.showAndWait();
            return;
        }

        try {
            if (editTarget == null) {

                Patient patient = new Patient(0, name, phone);
                patientDAO.insert(patient);

                Appointment appt = new Appointment(
                        0, patient.getPatientId(), name, phone,
                        date, time, doctor, reason,
                        status != null ? status : "Scheduled");

                appointmentDAO.insert(appt);

            } else {

                Patient patient = new Patient(
                        editTarget.getPatientId(), name, phone);
                patientDAO.update(patient);

                editTarget.setPatientName(name);
                editTarget.setPhone(phone);
                editTarget.setDate(date);
                editTarget.setTime(time);
                editTarget.setDoctor(doctor);
                editTarget.setReason(reason);
                editTarget.setStatus(status);

                appointmentDAO.update(editTarget);
            }

            if (mainController != null) {
                mainController.loadAppointmentsFromDB();
            }

            closeWindow();

        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Database Error");
            a.setHeaderText("Could not save");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------
    private void validateNameInline() {
        lblErrName.setText(orEmpty(InputValidator.validateName(fieldName.getText())));
    }

    private void validatePhoneInline() {
        lblErrPhone.setText(orEmpty(InputValidator.validatePhone(fieldPhone.getText())));
    }

    private void validateDateInline() {
        lblErrDate.setText(orEmpty(InputValidator.validateDate(fieldDate.getValue())));
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

    private String orEmpty(String s) {
        return s != null ? s : "";
    }

    // -------------------------------------------------------------------------
    // CLOSE
    // -------------------------------------------------------------------------
    private void closeWindow() {
        ((Stage) fieldName.getScene().getWindow()).close();
    }
}