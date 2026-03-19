package com.example.healthappointment.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.healthappointment.model.Appointment;

import java.time.LocalDate;

public class MainController {

    @FXML
    private TableView<Appointment> tableView;
    @FXML
    private TableColumn<Appointment, String> patientIdColumn;
    @FXML
    private TableColumn<Appointment, String> patientNameColumn;
    @FXML
    private TableColumn<Appointment, LocalDate> dateColumn;
    @FXML
    private TableColumn<Appointment, String> timeColumn;
    @FXML
    private TableColumn<Appointment, String> doctorColumn;
    @FXML
    private TableColumn<Appointment, String> conditionColumn;

    @FXML
    private TextField patientIdField;
    @FXML
    private TextField patientNameField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<String> timeChoiceBox;
    @FXML
    private ChoiceBox<String> doctorChoiceBox;
    @FXML
    private ChoiceBox<String> conditionChoiceBox;

    private ObservableList<Appointment> appointments;

    @FXML
    public void initialize() {
        // Set up TableView columns
        patientIdColumn.setCellValueFactory(data -> data.getValue().patientIdProperty());
        patientNameColumn.setCellValueFactory(data -> data.getValue().patientNameProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        timeColumn.setCellValueFactory(data -> data.getValue().timeProperty());
        doctorColumn.setCellValueFactory(data -> data.getValue().doctorProperty());
        conditionColumn.setCellValueFactory(data -> data.getValue().conditionProperty());

        appointments = FXCollections.observableArrayList();
        tableView.setItems(appointments);

        // Populate dropdowns
        timeChoiceBox.setItems(FXCollections.observableArrayList(
                "09:00 AM", "10:00 AM", "11:00 AM", "01:00 PM", "02:00 PM", "03:00 PM"
        ));
        doctorChoiceBox.setItems(FXCollections.observableArrayList(
                "Dr. Smith", "Dr. Johnson", "Dr. Lee"
        ));
        conditionChoiceBox.setItems(FXCollections.observableArrayList(
                "Flu", "Headache", "Back Pain", "Allergy", "Diabetes"
        ));

        // Fill form when a row is selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                patientIdField.setText(newSelection.getPatientId());
                patientNameField.setText(newSelection.getPatientName());
                datePicker.setValue(newSelection.getDate());
                timeChoiceBox.setValue(newSelection.getTime());
                doctorChoiceBox.setValue(newSelection.getDoctor());
                conditionChoiceBox.setValue(newSelection.getCondition());
            } else {
                clearFields();
            }
        });
    }

    // Validate all input fields
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (patientIdField.getText().isEmpty()) errors.append("Patient ID is required.\n");
        if (patientNameField.getText().isEmpty()) errors.append("Patient Name is required.\n");
        if (datePicker.getValue() == null) errors.append("Date is required.\n");
        if (timeChoiceBox.getValue() == null) errors.append("Time is required.\n");
        if (doctorChoiceBox.getValue() == null) errors.append("Doctor is required.\n");
        if (conditionChoiceBox.getValue() == null) errors.append("Condition is required.\n");

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!validateFields()) return;

        Appointment a = new Appointment(
                patientIdField.getText(),
                patientNameField.getText(),
                datePicker.getValue(),
                timeChoiceBox.getValue(),
                doctorChoiceBox.getValue(),
                conditionChoiceBox.getValue()
        );
        appointments.add(a);
        clearFields();
    }

    @FXML
    private void handleUpdate() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please select an appointment to update.");
            alert.showAndWait();
            return;
        }

        if (!validateFields()) return;

        selected.setPatientId(patientIdField.getText());
        selected.setPatientName(patientNameField.getText());
        selected.setDate(datePicker.getValue());
        selected.setTime(timeChoiceBox.getValue());
        selected.setDoctor(doctorChoiceBox.getValue());
        selected.setCondition(conditionChoiceBox.getValue());

        tableView.refresh();
        clearFields();
    }

    @FXML
    private void handleDelete() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) appointments.remove(selected);
        clearFields();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void clearFields() {
        patientIdField.clear();
        patientNameField.clear();
        datePicker.setValue(null);
        timeChoiceBox.setValue(null);
        doctorChoiceBox.setValue(null);
        conditionChoiceBox.setValue(null);
    }
}