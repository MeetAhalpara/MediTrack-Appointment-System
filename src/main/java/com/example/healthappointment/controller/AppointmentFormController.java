package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.GlassButtonAnimator;
import com.example.healthappointment.util.InputValidator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

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
    @FXML private VBox root;

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

        setupDatePickerConstraints();
        setupEnterNavigation();

        // Inline validation
        fieldName.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validateNameInline(); });

        fieldPhone.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validatePhoneInline(); });

        fieldDate.focusedProperty().addListener(
                (obs, oldV, newV) -> { if (!newV) validateDateInline(); });

        setupAppWideButtonAnimation();
    }

    private void setupAppWideButtonAnimation() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));
        });
        Platform.runLater(() -> GlassButtonAnimator.applyToButtons(root));
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

            String formatted;
            if (digits.length() <= 3) {
                formatted = digits;
            } else if (digits.length() <= 6) {
                formatted = "(" + digits.substring(0, 3) + ") " + digits.substring(3);
            } else {
                formatted = "(" + digits.substring(0, 3) + ") " + digits.substring(3, 6) + "-" + digits.substring(6);
            }

            // Prevent loop
            if (!formatted.equals(newValue)) {
                fieldPhone.setText(formatted);
                fieldPhone.positionCaret(formatted.length());
            }
        });
    }

    private void setupDatePickerConstraints() {
        fieldDate.setEditable(false);
        fieldDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });
    }

    private void setupEnterNavigation() {
        bindEnterToNext(fieldName, fieldPhone);
        bindEnterToNext(fieldPhone, fieldDate);
        bindEnterToNext(fieldDate, fieldTime);
        bindEnterToNext(fieldTime, fieldDoctor);
        bindEnterToNext(fieldDoctor, fieldReason);
        bindEnterToNext(fieldReason, fieldStatus);
        bindEnterToNext(fieldStatus, null);

        fieldDate.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.ENTER) {
                evt.consume();
                fieldTime.requestFocus();
                fieldTime.show();
            }
        });
    }

    private void bindEnterToNext(Control current, Control next) {
        current.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() != KeyCode.ENTER) {
                return;
            }

            evt.consume();
            if (next == null) {
                handleSave();
                return;
            }

            next.requestFocus();
            if (next instanceof ComboBox<?> comboBox) {
                comboBox.show();
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

            showValidationError(errors);
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
        String error = InputValidator.validateName(fieldName.getText());
        lblErrName.setText(orEmpty(error));
        setErrorState(fieldName, error != null);
    }

    private void validatePhoneInline() {
        String error = InputValidator.validatePhone(fieldPhone.getText());
        lblErrPhone.setText(orEmpty(error));
        setErrorState(fieldPhone, error != null);
    }

    private void validateDateInline() {
        String error = InputValidator.validateDate(fieldDate.getValue());
        lblErrDate.setText(orEmpty(error));
        setErrorState(fieldDate, error != null);
    }

    private void showInlineErrors(String name, String phone, LocalDate date,
                                  String time, String doctor, String reason) {
        String nameError = InputValidator.validateName(name);
        String phoneError = InputValidator.validatePhone(phone);
        String dateError = InputValidator.validateDate(date);
        String timeError = InputValidator.validateTime(time);
        String doctorError = InputValidator.validateDoctor(doctor);
        String reasonError = InputValidator.validateReason(reason);

        lblErrName.setText(orEmpty(nameError));
        lblErrPhone.setText(orEmpty(phoneError));
        lblErrDate.setText(orEmpty(dateError));
        lblErrTime.setText(orEmpty(timeError));
        lblErrDoctor.setText(orEmpty(doctorError));
        lblErrReason.setText(orEmpty(reasonError));

        setErrorState(fieldName, nameError != null);
        setErrorState(fieldPhone, phoneError != null);
        setErrorState(fieldDate, dateError != null);
        setErrorState(fieldTime, timeError != null);
        setErrorState(fieldDoctor, doctorError != null);
        setErrorState(fieldReason, reasonError != null);
    }

    private void clearErrors() {
        lblErrName.setText("");
        lblErrPhone.setText("");
        lblErrDate.setText("");
        lblErrTime.setText("");
        lblErrDoctor.setText("");
        lblErrReason.setText("");

        setErrorState(fieldName, false);
        setErrorState(fieldPhone, false);
        setErrorState(fieldDate, false);
        setErrorState(fieldTime, false);
        setErrorState(fieldDoctor, false);
        setErrorState(fieldReason, false);
    }

    private void setErrorState(Control control, boolean hasError) {
        if (control == null) {
            return;
        }
        if (hasError) {
            if (!control.getStyleClass().contains("input-error")) {
                control.getStyleClass().add("input-error");
            }
            animateInvalidControl(control);
            return;
        }
        control.getStyleClass().remove("input-error");
        control.getStyleClass().remove("input-error-animated");
    }

    private void showValidationError(String errors) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Validation Error");

        Label title = new Label("Please complete the missing information");
        title.getStyleClass().add("validation-title");

        Label subtitle = new Label("The appointment cannot be saved until all required details are valid.");
        subtitle.getStyleClass().add("validation-subtitle");
        subtitle.setWrapText(true);

        VBox listBox = new VBox(6);
        listBox.getStyleClass().add("validation-list");

        for (String line : errors.split("\\R")) {
            String text = line == null ? "" : line.trim();
            if (text.isEmpty()) {
                continue;
            }
            if (text.startsWith("•")) {
                text = text.substring(1).trim();
            }

            Label item = new Label("• " + text);
            item.setWrapText(true);
            item.getStyleClass().add("validation-item");
            listBox.getChildren().add(item);
        }

        VBox content = new VBox(10, title, subtitle, listBox);
        content.getStyleClass().add("validation-dialog-content");

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Window owner = fieldName != null && fieldName.getScene() != null ? fieldName.getScene().getWindow() : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setGraphic(null);
        if (!pane.getStyleClass().contains("app-dialog")) {
            pane.getStyleClass().add("app-dialog");
        }
        if (!pane.getStyleClass().contains("validation-dialog")) {
            pane.getStyleClass().add("validation-dialog");
        }

        String cssPath = getClass().getResource("/com/example/healthappointment/styles.css").toExternalForm();
        if (!pane.getStylesheets().contains(cssPath)) {
            pane.getStylesheets().add(cssPath);
        }

        dialog.showAndWait();
    }

    private void animateInvalidControl(Control control) {
        Animation running = (Animation) control.getProperties().get("invalidAnimation");
        if (running != null) {
            running.stop();
        }

        if (!control.getStyleClass().contains("input-error-animated")) {
            control.getStyleClass().add("input-error-animated");
        }

        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(control.translateXProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(55), new KeyValue(control.translateXProperty(), -5.5, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(110), new KeyValue(control.translateXProperty(), 5.5, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(170), new KeyValue(control.translateXProperty(), -4.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(225), new KeyValue(control.translateXProperty(), 4.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(280), new KeyValue(control.translateXProperty(), 0, Interpolator.EASE_BOTH))
        );

        ScaleTransition pulse = new ScaleTransition(Duration.millis(140), control);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.012);
        pulse.setToY(1.012);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition invalidFx = new ParallelTransition(shake, pulse);
        invalidFx.setOnFinished(evt -> {
            control.setTranslateX(0);
            control.setScaleX(1.0);
            control.setScaleY(1.0);
            control.getStyleClass().remove("input-error-animated");
            control.getProperties().remove("invalidAnimation");
        });

        control.getProperties().put("invalidAnimation", invalidFx);
        invalidFx.play();
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