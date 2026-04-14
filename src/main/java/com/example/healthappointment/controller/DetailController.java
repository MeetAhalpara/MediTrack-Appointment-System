package com.example.healthappointment.controller;

import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.util.GlassButtonAnimator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * DetailController — read-only Appointment Details screen.
 *
 * Accessibility enhanced version
 */
public class DetailController {

    @FXML private VBox root;

    @FXML private Label lblAppointmentId;
    @FXML private Label lblPatientName;
    @FXML private Label lblPhone;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblDoctor;
    @FXML private Label lblReason;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {

        // Apply animations (existing)
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));
        });
        Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));

        // Accessibility setup
        setupAccessibility();
    }

    // -------------------------------------------------------------------------
    // ACCESSIBILITY SETUP
    // -------------------------------------------------------------------------
    private void setupAccessibility() {

        root.setAccessibleText("Appointment details screen");
        root.setAccessibleHelp("Displays detailed information about a selected appointment");

        lblAppointmentId.setAccessibleText("Appointment ID");
        lblPatientName.setAccessibleText("Patient name");
        lblPhone.setAccessibleText("Phone number");
        lblDate.setAccessibleText("Appointment date");
        lblTime.setAccessibleText("Appointment time");
        lblDoctor.setAccessibleText("Doctor name");
        lblReason.setAccessibleText("Reason for appointment");
        lblStatus.setAccessibleText("Appointment status");

        // Ensure focusable for keyboard users
        root.setFocusTraversable(true);
    }

    /**
     * Populates the view with data from the given appointment.
     */
    public void setAppointment(Appointment appt) {

        lblAppointmentId.setText(String.valueOf(appt.getAppointmentId()));
        lblPatientName.setText(appt.getPatientName());
        lblPhone.setText(appt.getPhone());
        lblDate.setText(appt.getDate() != null ? appt.getDate().toString() : "—");
        lblTime.setText(appt.getTime());
        lblDoctor.setText(appt.getDoctor());
        lblReason.setText(appt.getReason());
        lblStatus.setText(appt.getStatus());

        //  Important: announce content for screen readers
        root.setAccessibleHelp(
                "Viewing appointment for " + appt.getPatientName() +
                        " on " + appt.getDate() +
                        " at " + appt.getTime()
        );
    }

    // -------------------------------------------------------------------------
    // CLOSE
    // -------------------------------------------------------------------------
    @FXML
    private void handleClose() {
        ((Stage) lblPatientName.getScene().getWindow()).close();
    }
}