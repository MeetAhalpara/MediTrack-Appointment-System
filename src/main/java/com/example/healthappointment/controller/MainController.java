package com.example.healthappointment.controller;

import com.example.healthappointment.dao.AppointmentDAO;
import com.example.healthappointment.model.Appointment;
import com.example.healthappointment.util.GlassButtonAnimator;
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
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
    @FXML private FlowPane filterToolbar;
    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;
    @FXML private HBox searchShell;
    @FXML private HBox dateGroup;
    @FXML private Button clearFilterButton;

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
    @FXML private Region underlineAdd;
    @FXML private Region underlineEdit;
    @FXML private Region underlineDelete;
    @FXML private Region underlineDetails;

    // -------------------------------------------------------------------------
    // DAO
    // -------------------------------------------------------------------------
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private static final double SIDEBAR_WIDTH = 240;
    private static final Duration SIDEBAR_OPEN_DURATION = Duration.millis(360);
    private static final Duration SIDEBAR_CLOSE_DURATION = Duration.millis(280);
    private static final Interpolator SIDEBAR_OPEN_EASING = Interpolator.SPLINE(0.18, 0.95, 0.18, 1.0);
    private static final Interpolator SIDEBAR_CLOSE_EASING = Interpolator.SPLINE(0.42, 0.0, 0.2, 1.0);

    private boolean sidebarOpen;
    private Timeline sidebarTimeline;
    private double latestSceneWidth = 1100;

    private static final String MODE_DESKTOP = "responsive-desktop";
    private static final String MODE_TABLET = "responsive-tablet";
    private static final String MODE_COMPACT = "responsive-compact";

    private final ObservableList<Appointment> masterList = FXCollections.observableArrayList();
    private final FilteredList<Appointment> filteredList = new FilteredList<>(masterList, appointment -> true);

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        setupColumns();
        setupFilterStatus();
        setupDatePicker();
        tableView.setItems(filteredList);
        loadAppointmentsFromDB();

        // Start with menu hidden and show it only when requested.
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebar.setTranslateX(-SIDEBAR_WIDTH);
        sidebar.setOpacity(0.0);
        sidebarOpen = false;
        resetHamburgerIcon();
        setupNavHoverAnimations();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        searchField.setPromptText("Type to filter appointments");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        setupResponsiveLayout();
        setupAppWideButtonAnimation();

        applyFilters();
    }

    private void setupAppWideButtonAnimation() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            Platform.runLater(() -> {
                GlassButtonAnimator.applyToButtons(rootPane);
                applyFilterBarGlassAnimation();
            });
        });
        Platform.runLater(() -> {
            GlassButtonAnimator.applyToButtons(rootPane);
            applyFilterBarGlassAnimation();
        });
    }

    private void applyFilterBarGlassAnimation() {
        // Keep filter controls visually consistent by avoiding extra glass layers.
        GlassButtonAnimator.applyToSurface(clearFilterButton, "glass-animated-button");
    }


    private void setupResponsiveLayout() {
        filterToolbar.widthProperty().addListener((obs, oldWidth, newWidth) ->
                updateResponsiveToolbar(newWidth.doubleValue()));

        tableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            latestSceneWidth = newScene.getWidth();
            newScene.widthProperty().addListener((sceneObs, oldWidth, newWidth) -> {
                latestSceneWidth = newWidth.doubleValue();
                applyResponsiveMode(latestSceneWidth);
            });
            applyResponsiveMode(latestSceneWidth);
        });

        Platform.runLater(() -> {
            Scene scene = tableView.getScene();
            if (scene != null) {
                latestSceneWidth = scene.getWidth();
                applyResponsiveMode(latestSceneWidth);
            }
            updateResponsiveToolbar(filterToolbar.getWidth());
        });
    }

    private void updateResponsiveToolbar(double width) {
        if (width <= 0 || filterToolbar == null || searchShell == null || dateGroup == null || filterDate == null || filterStatus == null || clearFilterButton == null) {
            return;
        }

        double usableWidth = Math.max(280, width - 26);
        filterToolbar.setPrefWrapLength(width);
        searchShell.setMinWidth(200);
        searchShell.setMaxWidth(Region.USE_PREF_SIZE);
        dateGroup.setMaxWidth(Region.USE_PREF_SIZE);
        filterStatus.setMaxWidth(Region.USE_PREF_SIZE);
        clearFilterButton.setMaxWidth(Region.USE_PREF_SIZE);

        if (width >= 1250) {
            filterToolbar.setHgap(12);
            filterToolbar.setVgap(10);
            filterToolbar.setAlignment(javafx.geometry.Pos.CENTER);
            searchShell.setMinWidth(400);
            searchShell.setPrefWidth(clamp(480, usableWidth * 0.46, 740));
            dateGroup.setPrefWidth(240);
            filterDate.setPrefWidth(145);
            filterStatus.setPrefWidth(230);
            clearFilterButton.setPrefWidth(108);
        } else if (width >= 950) {
            filterToolbar.setHgap(10);
            filterToolbar.setVgap(10);
            filterToolbar.setAlignment(javafx.geometry.Pos.CENTER);
            searchShell.setMinWidth(300);
            searchShell.setPrefWidth(clamp(360, usableWidth * 0.43, 520));
            dateGroup.setPrefWidth(220);
            filterDate.setPrefWidth(130);
            filterStatus.setPrefWidth(205);
            clearFilterButton.setPrefWidth(96);
        } else if (width >= 700) {
            filterToolbar.setHgap(8);
            filterToolbar.setVgap(9);
            filterToolbar.setAlignment(javafx.geometry.Pos.CENTER);
            searchShell.setMinWidth(width - 36);
            searchShell.setPrefWidth(usableWidth);
            dateGroup.setPrefWidth(clamp(220, usableWidth * 0.46, 300));
            filterDate.setPrefWidth(140);
            filterStatus.setPrefWidth(clamp(180, usableWidth * 0.34, 260));
            clearFilterButton.setPrefWidth(96);
        } else {
            filterToolbar.setHgap(8);
            filterToolbar.setVgap(8);
            filterToolbar.setAlignment(javafx.geometry.Pos.CENTER);
            searchShell.setMinWidth(Math.max(220, width - 26));
            searchShell.setPrefWidth(usableWidth);

            if (width <= 560) {
                dateGroup.setPrefWidth(usableWidth);
                filterDate.setPrefWidth(Math.max(120, usableWidth - 110));
                filterStatus.setPrefWidth(usableWidth);
                clearFilterButton.setPrefWidth(usableWidth);
            } else {
                dateGroup.setPrefWidth(clamp(210, usableWidth * 0.50, 280));
                filterDate.setPrefWidth(Math.max(120, dateGroup.getPrefWidth() - 110));
                filterStatus.setPrefWidth(clamp(180, usableWidth * 0.38, 250));
                clearFilterButton.setPrefWidth(clamp(94, usableWidth * 0.2, 130));
            }
        }
    }

    private double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void applyResponsiveMode(double sceneWidth) {
        if (sceneWidth <= 0) {
            return;
        }

        rootPane.getStyleClass().removeAll(MODE_DESKTOP, MODE_TABLET, MODE_COMPACT);

        if (sceneWidth < 760) {
            rootPane.getStyleClass().add(MODE_COMPACT);
        } else if (sceneWidth < 1100) {
            rootPane.getStyleClass().add(MODE_TABLET);
        } else {
            rootPane.getStyleClass().add(MODE_DESKTOP);
        }

        if (sceneWidth < 880 && sidebarOpen) {
            sidebarOpen = false;
            animateHamburgerIcon(false);
            playSidebarTransition(false);
        }

        updateResponsiveToolbar(filterToolbar.getWidth());
    }

    // -------------------------------------------------------------------------
    // TABLE SETUP
    // -------------------------------------------------------------------------
    private void setupColumns() {
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
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
                "Scheduled", "Completed", "Cancelled"));
        filterStatus.setPromptText("Appointment Status");
        filterStatus.setValue(null);
    }

    private void setupDatePicker() {
        filterDate.setPromptText("");
        filterDate.setEditable(false);
        filterDate.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                if (!dateGroup.getStyleClass().contains("focus-within")) {
                    dateGroup.getStyleClass().add("focus-within");
                }
            } else {
                dateGroup.getStyleClass().remove("focus-within");
            }
        });
        filterDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });
    }

    // -------------------------------------------------------------------------
    // LOAD DATA
    // -------------------------------------------------------------------------
    public void loadAppointmentsFromDB() {
        try {
            List<Appointment> list = appointmentDAO.findAll();
            masterList.setAll(list);
            applyFilters();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // LIVE FILTERS
    // -------------------------------------------------------------------------
    private void applyFilters() {
        String keyword = searchField.getText();
        LocalDate date = filterDate.getValue();
        String status = filterStatus.getValue();

        filteredList.setPredicate(appointment -> {
            if (appointment == null) {
                return false;
            }

            if (keyword != null && !keyword.isBlank() && !matchesKeyword(appointment, keyword)) {
                return false;
            }

            if (date != null && !date.equals(appointment.getDate())) {
                return false;
            }

            return matchesStatus(appointment.getStatus(), status);
        });
    }

    @FXML
    private void handleClearFilter() {
        searchField.clear();
        filterDate.setValue(null);
        filterStatus.setValue(null);
        applyFilters();
    }

    private boolean matchesKeyword(Appointment appointment, String keyword) {
        String query = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(appointment.getPatientName(), query)
                || containsIgnoreCase(appointment.getDoctor(), query)
                || containsIgnoreCase(appointment.getPhone(), query)
                || containsIgnoreCase(appointment.getReason(), query)
                || containsIgnoreCase(appointment.getStatus(), query)
                || String.valueOf(appointment.getAppointmentId()).contains(query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean matchesStatus(String appointmentStatus, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.isBlank()) {
            return true;
        }
        return appointmentStatus != null
                && appointmentStatus.toLowerCase(Locale.ROOT).equals(selectedStatus.toLowerCase(Locale.ROOT));
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
            showNoSelectionWarning();
            return;
        }
        openForm(selected);
    }

    @FXML
    private void handleDelete() {
        Appointment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNoSelectionWarning();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete appointment?");
        styleAlert(confirm);
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
            showNoSelectionWarning();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/healthappointment/detail-view.fxml"));

            Parent root = loader.load();
            DetailController controller = loader.getController();
            controller.setAppointment(selected);

            Stage stage = createPopupStage(root, "Appointment Details", 560, 520);
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
            sidebar.setMinWidth(SIDEBAR_WIDTH);
            sidebar.setPrefWidth(SIDEBAR_WIDTH);
            sidebar.setMaxWidth(SIDEBAR_WIDTH);
            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            sidebar.setOpacity(0.0);
        }

        Duration duration = opening ? SIDEBAR_OPEN_DURATION : SIDEBAR_CLOSE_DURATION;
        Interpolator easing = opening ? SIDEBAR_OPEN_EASING : SIDEBAR_CLOSE_EASING;

        double targetX = opening ? 0 : -SIDEBAR_WIDTH;
        double targetOpacity = opening ? 1.0 : 0.0;

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
                sidebar.setTranslateX(-SIDEBAR_WIDTH);
                sidebar.setOpacity(0.0);
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
    }

    private void setupNavButtonAnimation(Button button, Region underline) {
        Rectangle clip = new Rectangle(0, 2);
        clip.setX(0);
        clip.heightProperty().bind(underline.heightProperty());
        underline.setClip(clip);
        underline.getProperties().put("clipRect", clip);

        button.widthProperty().addListener((obs, oldVal, newVal) ->
                updateUnderlineWidth(underline, newVal.doubleValue()));
        button.layoutBoundsProperty().addListener((obs, oldVal, newVal) ->
                updateUnderlineWidth(underline, newVal.getWidth()));

        Platform.runLater(() -> updateUnderlineWidth(underline,
                Math.max(button.getWidth(), button.getLayoutBounds().getWidth())));
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
        if (underline == null) return;
        Rectangle clip = (Rectangle) underline.getProperties().get("clipRect");
        if (clip == null) {
            return;
        }

        Timeline running = (Timeline) underline.getProperties().get("underlineTimeline");
        if (running != null) {
            running.stop();
        }

        double targetWidth = show ? Math.max(underline.getWidth(), underline.getPrefWidth()) : 0;
        if (targetWidth == 0 && show) targetWidth = 160; // Fallback

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(show ? 320 : 220),
                        new KeyValue(clip.widthProperty(), targetWidth, Interpolator.EASE_BOTH),
                        new KeyValue(underline.opacityProperty(), show ? 1.0 : 0.0, Interpolator.EASE_BOTH)
                )
        );
        underline.getProperties().put("underlineTimeline", timeline);
        timeline.play();
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

            Stage stage = createPopupStage(root, appt == null ? "Add Appointment" : "Edit Appointment", 640, 690);
            stage.showAndWait();

        } catch (IOException e) {
            showError("Error", e.getMessage());
        }
    }

    private Stage createPopupStage(Parent contentRoot, String title, double width, double height) {
        Stage stage = new Stage();
        Scene scene = new Scene(contentRoot, width, height);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(true);
        stage.setMinWidth(Math.max(480, width * 0.82));
        stage.setMinHeight(Math.max(440, height * 0.82));
        stage.initModality(Modality.WINDOW_MODAL);

        Window owner = rootPane != null && rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setOnShowing(evt -> {
            contentRoot.setOpacity(0.0);
            contentRoot.setTranslateY(18);
            contentRoot.setScaleX(0.985);
            contentRoot.setScaleY(0.985);
        });
        stage.setOnShown(evt -> {
            FadeTransition fade = new FadeTransition(Duration.millis(240), contentRoot);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition slide = new TranslateTransition(Duration.millis(240), contentRoot);
            slide.setFromY(18);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0));

            Timeline scale = new Timeline(
                    new KeyFrame(Duration.millis(240),
                            new KeyValue(contentRoot.scaleXProperty(), 1.0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0)),
                            new KeyValue(contentRoot.scaleYProperty(), 1.0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0))
                    )
            );

            new ParallelTransition(fade, slide, scale).play();
        });

        final boolean[] closingAnimated = {false};
        stage.setOnCloseRequest(evt -> {
            if (closingAnimated[0]) {
                return;
            }

            evt.consume();
            closingAnimated[0] = true;

            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), contentRoot);
            fadeOut.setFromValue(contentRoot.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_IN);

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(180), contentRoot);
            slideOut.setFromY(contentRoot.getTranslateY());
            slideOut.setToY(contentRoot.getTranslateY() + 12);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            Timeline scaleOut = new Timeline(
                    new KeyFrame(Duration.millis(180),
                            new KeyValue(contentRoot.scaleXProperty(), 0.985, Interpolator.EASE_IN),
                            new KeyValue(contentRoot.scaleYProperty(), 0.985, Interpolator.EASE_IN)
                    )
            );

            ParallelTransition exit = new ParallelTransition(fadeOut, slideOut, scaleOut);
            exit.setOnFinished(done -> stage.close());
            exit.play();
        });

        return stage;
    }

    // -------------------------------------------------------------------------
    // ALERTS
    // -------------------------------------------------------------------------
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void showNoSelectionWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("No Selection");
        alert.setContentText("Select appointment first.");
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        Window owner = rootPane != null && rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }

        DialogPane pane = alert.getDialogPane();
        if (pane != null) {
            pane.setGraphic(null);
            String cssPath = getClass().getResource("/com/example/healthappointment/styles.css").toExternalForm();
            if (!pane.getStylesheets().contains(cssPath)) {
                pane.getStylesheets().add(cssPath);
            }
            if (!pane.getStyleClass().contains("app-dialog")) {
                pane.getStyleClass().add("app-dialog");
            }
        }
    }
}

