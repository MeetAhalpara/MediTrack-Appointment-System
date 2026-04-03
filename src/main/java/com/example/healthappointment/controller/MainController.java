package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.dao.PatientDAO;
import com.example.healthappointment.model.Appointment;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    // Sidebar container toggled by hamburger button.
    @FXML private VBox sidebar;
    @FXML private Button hamburgerButton;
    @FXML private Region lineTop;
    @FXML private Region lineMiddle;
    @FXML private Region lineBottom;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnDetails;
    @FXML private Button btnExit;
    @FXML private Region underlineAdd;
    @FXML private Region underlineEdit;
    @FXML private Region underlineDelete;
    @FXML private Region underlineDetails;
    @FXML private Region underlineExit;

    // -------------------------------------------------------------------------
    // DAO
    // -------------------------------------------------------------------------
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private static final double SIDEBAR_WIDTH = 220;
    private static final Duration SIDEBAR_OPEN_DURATION = Duration.millis(300);
    private static final Duration SIDEBAR_CLOSE_DURATION = Duration.millis(240);
    private static final Interpolator SIDEBAR_OPEN_EASING = Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0);
    private static final Interpolator SIDEBAR_CLOSE_EASING = Interpolator.SPLINE(0.4, 0.0, 1.0, 1.0);

    private boolean sidebarOpen;
    private Timeline sidebarTimeline;

    private ObservableList<Appointment> masterList = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        setupColumns();
        setupFilterStatus();
        loadAppointmentsFromDB();

        // Start with menu hidden and show it only when requested.
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        sidebar.setTranslateX(-SIDEBAR_WIDTH);
        sidebar.setOpacity(1.0);
        sidebarOpen = false;
        resetHamburgerIcon();
        setupNavHoverAnimations();

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
    private void handleToggleSidebar() {
        boolean shouldShow = !sidebarOpen;
        sidebarOpen = shouldShow;
        animateHamburgerIcon(shouldShow);

        playSidebarTransition(shouldShow);
    }

    private void playSidebarTransition(boolean opening) {
        if (sidebarTimeline != null) {
            sidebarTimeline.stop();
        }

        if (opening) {
            sidebar.setManaged(true);
            sidebar.setVisible(true);
        }

        Duration duration = opening ? SIDEBAR_OPEN_DURATION : SIDEBAR_CLOSE_DURATION;
        Interpolator easing = opening ? SIDEBAR_OPEN_EASING : SIDEBAR_CLOSE_EASING;

        double targetX = opening ? 0 : -SIDEBAR_WIDTH;
        double targetOpacity = opening ? 1.0 : 0.92;

        sidebarTimeline = new Timeline(
                new KeyFrame(
                        duration,
                        new KeyValue(sidebar.translateXProperty(), targetX, easing),
                        new KeyValue(sidebar.opacityProperty(), targetOpacity, easing)
                )
        );

        sidebarTimeline.setOnFinished(evt -> {
            if (!opening) {
                sidebar.setVisible(false);
                sidebar.setManaged(false);
            }
        });
        sidebarTimeline.play();
    }

    private void resetHamburgerIcon() {
        lineTop.setRotate(0);
        lineTop.setTranslateY(0);
        lineMiddle.setOpacity(1);
        lineBottom.setRotate(0);
        lineBottom.setTranslateY(0);
        hamburgerButton.getStyleClass().remove("open");
    }

    private void animateHamburgerIcon(boolean menuOpen) {
        Duration duration = Duration.millis(240);
        if (menuOpen && !hamburgerButton.getStyleClass().contains("open")) {
            hamburgerButton.getStyleClass().add("open");
        } else if (!menuOpen) {
            hamburgerButton.getStyleClass().remove("open");
        }

        TranslateTransition topMove = new TranslateTransition(duration, lineTop);
        topMove.setToY(menuOpen ? 6 : 0);
        topMove.setInterpolator(Interpolator.EASE_BOTH);
        RotateTransition topRotate = new RotateTransition(duration, lineTop);
        topRotate.setToAngle(menuOpen ? 45 : 0);
        topRotate.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition middleFade = new FadeTransition(duration, lineMiddle);
        middleFade.setToValue(menuOpen ? 0 : 1);
        middleFade.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition bottomMove = new TranslateTransition(duration, lineBottom);
        bottomMove.setToY(menuOpen ? -6 : 0);
        bottomMove.setInterpolator(Interpolator.EASE_BOTH);
        RotateTransition bottomRotate = new RotateTransition(duration, lineBottom);
        bottomRotate.setToAngle(menuOpen ? -45 : 0);
        bottomRotate.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(topMove, topRotate, middleFade, bottomMove, bottomRotate).play();
    }

    private void setupNavHoverAnimations() {
        setupNavButtonAnimation(btnAdd, underlineAdd);
        setupNavButtonAnimation(btnEdit, underlineEdit);
        setupNavButtonAnimation(btnDelete, underlineDelete);
        setupNavButtonAnimation(btnDetails, underlineDetails);
        setupNavButtonAnimation(btnExit, underlineExit);
    }

    private void setupNavButtonAnimation(Button button, Region underline) {
        Rectangle clip = new Rectangle(0, 2);
        clip.setX(0);
        clip.heightProperty().bind(underline.heightProperty());
        underline.setClip(clip);
        underline.getProperties().put("clipRect", clip);

        button.widthProperty().addListener((obs, oldVal, newVal) ->
                updateUnderlineWidth(underline, newVal.doubleValue()));

        Platform.runLater(() -> updateUnderlineWidth(underline, button.getWidth()));
        underline.setOpacity(0);

        button.setOnMouseEntered(evt -> animateUnderline(underline, true));
        button.setOnMouseExited(evt -> animateUnderline(underline, false));
    }

    private void updateUnderlineWidth(Region underline, double buttonWidth) {
        double targetWidth = Math.max(0, buttonWidth);
        underline.setPrefWidth(targetWidth);
        underline.setMinWidth(targetWidth);
        underline.setMaxWidth(targetWidth);
    }

    private void animateUnderline(Region underline, boolean show) {
        Rectangle clip = (Rectangle) underline.getProperties().get("clipRect");
        if (clip == null) {
            return;
        }

        Timeline running = (Timeline) underline.getProperties().get("underlineTimeline");
        if (running != null) {
            running.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(show ? 260 : 190),
                        new KeyValue(clip.widthProperty(), show ? underline.getPrefWidth() : 0, Interpolator.EASE_BOTH),
                        new KeyValue(underline.opacityProperty(), show ? 1.0 : 0.0, Interpolator.EASE_BOTH)
                )
        );
        underline.getProperties().put("underlineTimeline", timeline);
        timeline.play();
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