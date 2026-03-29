package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.model.Patient;
import com.example.healthappointment.util.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * MainController — Dashboard controller.
 *
 * Responsibilities (from proposal use cases 1–5):
 *  UC1  Add new appointment (opens AppointmentFormController in a modal)
 *  UC2  Load + display all appointments from database on startup
 *  UC3  Update selected appointment (opens same form pre-filled)
 *  UC4  Delete selected appointment
 *  UC5  Real-time search by patient name or doctor
 *       Compound filter by date and/or status
 *  UC6  View Details button — opens read-only detail window
 */
public class MainController {

    // -------------------------------------------------------------------------
    // FXML — TableView
    // -------------------------------------------------------------------------
    @FXML private TableView<Appointment>                     tableView;
    @FXML private TableColumn<Appointment, Integer>          colId;
    @FXML private TableColumn<Appointment, String>           colPatientName;
    @FXML private TableColumn<Appointment, String>           colPhone;
    @FXML private TableColumn<Appointment, LocalDate>        colDate;
    @FXML private TableColumn<Appointment, String>           colTime;
    @FXML private TableColumn<Appointment, String>           colDoctor;
    @FXML private TableColumn<Appointment, String>           colReason;
    @FXML private TableColumn<Appointment, String>           colStatus;

    // -------------------------------------------------------------------------
    // FXML — Search & Filter bar (proposal Section 9)
    // -------------------------------------------------------------------------
    @FXML private TextField   searchField;
    @FXML private DatePicker  filterDate;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Button      btnClearFilter;

    // -------------------------------------------------------------------------
    // DAO layer
    // -------------------------------------------------------------------------
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO     patientDAO     = new PatientDAO();

    private ObservableList<Appointment> masterList = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        setupColumns();
        setupFilterStatus();
        loadAppointmentsFromDB();

        // Real-time search listener (proposal Section 9)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch(newVal));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Colour-code status column for visual clarity
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle(switch (status) {
                        case "Scheduled" -> "-fx-text-fill: #1565C0; -fx-font-weight: bold;";
                        case "Completed" -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
                        case "Cancelled" -> "-fx-text-fill: #C62828; -fx-font-weight: bold;";
                        default          -> "";
                    });
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
    // Database load
    // -------------------------------------------------------------------------

    /** Fetches all appointments from DB and refreshes the TableView. */
    public void loadAppointmentsFromDB() {
        try {
            List<Appointment> list = appointmentDAO.findAll();
            masterList.setAll(list);
            tableView.setItems(masterList);
        } catch (SQLException e) {
            showError("Database Error", "Failed to load appointments:\n" + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Search & Filter (proposal Section 9)
    // -------------------------------------------------------------------------

    /** Called on every keystroke in the search field. */
    private void applySearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            tableView.setItems(masterList);
            return;
        }
        try {
            List<Appointment> results = appointmentDAO.searchByNameOrDoctor(keyword.trim());
            tableView.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        LocalDate date   = filterDate.getValue();
        String    status = filterStatus.getValue();
        try {
            List<Appointment> results = appointmentDAO.filterByDateAndStatus(
                    date, (status == null || status.isEmpty()) ? null : status);
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
    // CRUD handlers
    // -------------------------------------------------------------------------

    /** UC1 — opens the Add Appointment modal form. */
    @FXML
    private void handleAdd() {
        openAppointmentForm(null);
    }

    /** UC3 — opens the Edit Appointment modal form pre-filled with selected row. */
    @FXML
    private void handleUpdate() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an appointment to edit.");
            return;
        }
        openAppointmentForm(selected);
    }

    /** UC4 — deletes the selected appointment after confirmation. */
    @FXML
    private void handleDelete() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an appointment to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete appointment for " + selected.getPatientName() + "?");
        confirm.setContentText("This action cannot be undone.");
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

    /** UC6 — opens the read-only Details view for the selected appointment. */
    @FXML
    private void handleViewDetails() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an appointment to view.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/healthappointment/detail-view.fxml"));
            Parent root  = loader.load();
            DetailController dc = loader.getController();
            dc.setAppointment(selected);

            Stage stage = new Stage();
            stage.setTitle("Appointment Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 500, 480));
            stage.showAndWait();
        } catch (IOException e) {
            showError("View Error", e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Opens the Add/Edit form as a modal dialog.
     *
     * @param appointment  null → Add mode; non-null → Edit mode
     */
    private void openAppointmentForm(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/healthappointment/appointment-form.fxml"));
            Parent root = loader.load();
            AppointmentFormController fc = loader.getController();
            fc.setMainController(this);
            fc.setAppointment(appointment);   // null = Add mode

            Stage stage = new Stage();
            stage.setTitle(appointment == null ? "Add Appointment" : "Edit Appointment");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 520, 560));
            stage.showAndWait();
        } catch (IOException e) {
            showError("Form Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Alert helpers (proposal Section 10)
    // -------------------------------------------------------------------------

    void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showWarning(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Warning");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}