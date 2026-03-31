package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainController {

    // -------------------------------------------------------------------------
    // TABLE
    // -------------------------------------------------------------------------
    @FXML private TableView<Appointment> tableView;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colPatientName;
    @FXML private TableColumn<Appointment, String> colPhone;
    @FXML private TableColumn<Appointment, LocalDate> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;

    // -------------------------------------------------------------------------
    // FILTER
    // -------------------------------------------------------------------------
    @FXML private TextField searchField;
    @FXML private DatePicker filterDate;
    @FXML private ComboBox<String> filterStatus;

    // -------------------------------------------------------------------------
    // DAO
    // -------------------------------------------------------------------------
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private ObservableList<Appointment> masterList = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        setupColumns();
        setupFilterStatus();
        loadAppointmentsFromDB();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch(newVal));
    }

    // -------------------------------------------------------------------------
    // TABLE SETUP
    // -------------------------------------------------------------------------
    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Colored status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);

                    switch (status) {
                        case "Scheduled":
                            setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                            break;
                        case "Completed":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "Cancelled":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupFilterStatus() {
        filterStatus.setItems(FXCollections.observableArrayList(
                "", "Scheduled", "Completed", "Cancelled"));
        filterStatus.setValue("");
    }

    // -------------------------------------------------------------------------
    // LOAD DATA
    // -------------------------------------------------------------------------
    public void loadAppointmentsFromDB() {
        try {
            List<Appointment> list = appointmentDAO.findAll();
            masterList.setAll(list);
            tableView.setItems(masterList);
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------------------
    private void applySearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            tableView.setItems(masterList);
            return;
        }

        try {
            List<Appointment> results =
                    appointmentDAO.searchByNameOrDoctor(keyword.trim());
            tableView.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // FILTER
    // -------------------------------------------------------------------------
    @FXML
    private void handleFilter() {
        LocalDate date = filterDate.getValue();
        String status = filterStatus.getValue();

        try {
            List<Appointment> results =
                    appointmentDAO.filterByDateAndStatus(
                            date,
                            (status == null || status.isEmpty()) ? null : status
                    );

            tableView.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            showError("Filter Error", e.getMessage());
        }
    }

    @FXML
    private void handleClearFilter() {
        searchField.clear();
        filterDate.setValue(null);
        filterStatus.setValue("");
        loadAppointmentsFromDB();
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------
    @FXML
    private void handleAdd() {
        openForm(null);
    }

    @FXML
    private void handleUpdate() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Select appointment first.");
            return;
        }
        openForm(selected);
    }

    @FXML
    private void handleDelete() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Select appointment first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete appointment?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    appointmentDAO.delete(selected.getAppointmentId());
                    loadAppointmentsFromDB();
                } catch (SQLException e) {
                    showError("Delete Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleViewDetails() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Select appointment first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/healthappointment/detail-view.fxml"));

            Parent root = loader.load();
            DetailController controller = loader.getController();
            controller.setAppointment(selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 500, 480));
            stage.setTitle("Appointment Details");
            stage.showAndWait();

        } catch (IOException e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // -------------------------------------------------------------------------
    // FORM
    // -------------------------------------------------------------------------
    private void openForm(Appointment appt) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/healthappointment/appointment-form.fxml"));

            Parent root = loader.load();
            AppointmentFormController controller = loader.getController();

            controller.setMainController(this);
            controller.setAppointment(appt);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 520, 560));
            stage.setTitle(appt == null ? "Add Appointment" : "Edit Appointment");
            stage.showAndWait();

        } catch (IOException e) {
            showError("Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ALERTS
    // -------------------------------------------------------------------------
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}