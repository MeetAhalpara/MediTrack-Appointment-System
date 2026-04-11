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
 * Maps to Use Case 6 (View Appointment Details) and Screen 3 in the
 * proposal (Section 3 / Section 4 UC6).
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
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));
        });
        Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));
    }

    /**
     * Populates the view with data from the given appointment.
     * Called by MainController immediately after loading the FXML.
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
    }

    @FXML
    private void handleClose() {
        ((Stage) lblPatientName.getScene().getWindow()).close();
    }
}