package com.example.healthappointment;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AccessibilityUITest extends BaseUiTest {

    private void ensureSidebarOpen() {
        VBox sidebar = lookup("#sidebar").queryAs(VBox.class);
        if (!sidebar.isVisible()) {
            clickOn("#hamburgerButton");
            sleep(900);
        }
    }

    private void ensureSidebarOpenAndReady() {
        ensureSidebarOpen();
        sleep(500);

        assertTrue(lookup("#btnAdd").tryQuery().isPresent());
        assertTrue(lookup("#btnEdit").tryQuery().isPresent());
        assertTrue(lookup("#btnDelete").tryQuery().isPresent());
        assertTrue(lookup("#btnDetails").tryQuery().isPresent());
    }

    private void openAddForm() {
        ensureSidebarOpenAndReady();
        clickOn("#btnAdd");
        waitForFormToBeReady();
    }

    private void waitForFormToBeReady() {
        sleep(1000);

        if (lookup("#fieldName").tryQuery().isEmpty()) {
            sleep(700);
        }

        assertTrue(lookup("#fieldName").tryQuery().isPresent(), "fieldName should be present");
        assertTrue(lookup("#saveButton").tryQuery().isPresent(), "saveButton should be present");
        assertTrue(lookup("#cancelButton").tryQuery().isPresent(), "cancelButton should be present");
    }

    private void fireSave() {
        interact(() -> lookup("#saveButton").queryAs(Button.class).fire());
        sleep(500);
    }

    private void closeFormSafely() {
        if (lookup("#cancelButton").tryQuery().isPresent()) {
            interact(() -> lookup("#cancelButton").queryAs(Button.class).fire());
            sleep(350);
        }
    }

    private DialogPane findDialogPaneIfPresent() {
        sleep(300);
        return lookup(".dialog-pane").tryQuery().map(node -> (DialogPane) node).orElse(null);
    }

    private void closeOkDialogIfPresent() {
        if (lookup("OK").tryQuery().isPresent()) {
            clickOn("OK");
            sleep(250);
        }
    }

    private void closeConfirmationDialogWithCancelIfPresent() {
        if (lookup(".dialog-pane").tryQuery().isPresent()) {
            DialogPane pane = (DialogPane) lookup(".dialog-pane").query();
            interact(() -> {
                Button cancelButton = (Button) pane.lookupButton(javafx.scene.control.ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.fire();
                }
            });
            sleep(300);
        }
    }

    private void seedAndSelectFirstRow() throws Exception {
        seedDefaultAppointment();
        refreshMainTable();
        sleep(500);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        assertFalse(tableView.getItems().isEmpty());

        interact(() -> {
            tableView.getSelectionModel().select(0);
            tableView.requestFocus();
        });
        sleep(250);
    }

    private void openDetailsForFirstRow() throws Exception {
        seedAppointment(
                "Emma Stone",
                "(613) 555-7878",
                LocalDate.now().plusDays(4),
                "11:00 AM",
                "Dr. Lee",
                "Review",
                "Scheduled"
        );

        refreshMainTable();
        sleep(500);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        assertFalse(tableView.getItems().isEmpty());

        interact(() -> {
            tableView.getSelectionModel().select(0);
            tableView.requestFocus();
        });
        sleep(250);

        ensureSidebarOpenAndReady();
        clickOn("#btnDetails");
        sleep(900);

        assertTrue(lookup("#lblPatientName").tryQuery().isPresent(), "Detail patient label should be present");
    }

    private void closeDetailsSafely() {
        if (lookup("#btnClose").tryQuery().isPresent()) {
            interact(() -> lookup("#btnClose").queryAs(Button.class).fire());
            sleep(300);
        }
    }

    @Test
    void mainScreen_controlsHaveAccessibleTextAndHelp() {
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);
        Button hamburgerButton = lookup("#hamburgerButton").queryAs(Button.class);

        assertEquals("Appointments table", tableView.getAccessibleText());
        assertNotNull(tableView.getAccessibleHelp());
        assertFalse(tableView.getAccessibleHelp().isBlank());

        assertEquals("Search appointments", searchField.getAccessibleText());
        assertNotNull(searchField.getAccessibleHelp());
        assertFalse(searchField.getAccessibleHelp().isBlank());

        assertEquals("Filter by date", filterDate.getAccessibleText());
        assertNotNull(filterDate.getAccessibleHelp());
        assertFalse(filterDate.getAccessibleHelp().isBlank());

        assertEquals("Filter by status", filterStatus.getAccessibleText());
        assertNotNull(filterStatus.getAccessibleHelp());
        assertFalse(filterStatus.getAccessibleHelp().isBlank());

        assertEquals("Toggle navigation menu", hamburgerButton.getAccessibleText());
        assertNotNull(hamburgerButton.getAccessibleHelp());
        assertFalse(hamburgerButton.getAccessibleHelp().isBlank());
    }

    @Test
    void sidebarButtons_haveAccessibleTextAndHelp() {
        ensureSidebarOpenAndReady();

        Button addButton = lookup("#btnAdd").queryAs(Button.class);
        Button editButton = lookup("#btnEdit").queryAs(Button.class);
        Button deleteButton = lookup("#btnDelete").queryAs(Button.class);
        Button detailsButton = lookup("#btnDetails").queryAs(Button.class);

        assertEquals("Add appointment", addButton.getAccessibleText());
        assertEquals("Edit appointment", editButton.getAccessibleText());
        assertEquals("Delete appointment", deleteButton.getAccessibleText());
        assertEquals("View appointment details", detailsButton.getAccessibleText());

        assertNotNull(addButton.getAccessibleHelp());
        assertNotNull(editButton.getAccessibleHelp());
        assertNotNull(deleteButton.getAccessibleHelp());
        assertNotNull(detailsButton.getAccessibleHelp());
    }

    @Test
    void mainScreen_controlsAreKeyboardFocusable() {
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);
        Button hamburgerButton = lookup("#hamburgerButton").queryAs(Button.class);

        assertTrue(tableView.isFocusTraversable());
        assertTrue(searchField.isFocusTraversable());
        assertTrue(filterDate.isFocusTraversable());
        assertTrue(filterStatus.isFocusTraversable());
        assertTrue(hamburgerButton.isFocusTraversable());
    }

    @Test
    void sidebarButtons_areKeyboardFocusable() {
        ensureSidebarOpenAndReady();

        Button addButton = lookup("#btnAdd").queryAs(Button.class);
        Button editButton = lookup("#btnEdit").queryAs(Button.class);
        Button deleteButton = lookup("#btnDelete").queryAs(Button.class);
        Button detailsButton = lookup("#btnDetails").queryAs(Button.class);

        assertTrue(addButton.isFocusTraversable());
        assertTrue(editButton.isFocusTraversable());
        assertTrue(deleteButton.isFocusTraversable());
        assertTrue(detailsButton.isFocusTraversable());
    }

    @Test
    void formFields_haveAccessibleTextAndHelp() {
        openAddForm();

        TextField nameField = lookup("#fieldName").queryAs(TextField.class);
        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);
        DatePicker dateField = lookup("#fieldDate").queryAs(DatePicker.class);
        ComboBox<?> timeField = lookup("#fieldTime").queryAs(ComboBox.class);
        ComboBox<?> doctorField = lookup("#fieldDoctor").queryAs(ComboBox.class);
        TextField reasonField = lookup("#fieldReason").queryAs(TextField.class);
        ComboBox<?> statusField = lookup("#fieldStatus").queryAs(ComboBox.class);

        assertEquals("Patient name", nameField.getAccessibleText());
        assertEquals("Phone number", phoneField.getAccessibleText());
        assertEquals("Appointment date", dateField.getAccessibleText());
        assertEquals("Appointment time", timeField.getAccessibleText());
        assertEquals("Doctor", doctorField.getAccessibleText());
        assertEquals("Reason for visit", reasonField.getAccessibleText());
        assertEquals("Appointment status", statusField.getAccessibleText());

        assertNotNull(nameField.getAccessibleHelp());
        assertNotNull(phoneField.getAccessibleHelp());
        assertNotNull(dateField.getAccessibleHelp());
        assertNotNull(timeField.getAccessibleHelp());
        assertNotNull(doctorField.getAccessibleHelp());
        assertNotNull(reasonField.getAccessibleHelp());
        assertNotNull(statusField.getAccessibleHelp());

        closeFormSafely();
    }

    @Test
    void formButtons_haveAccessibleTextHelpAndRoleDescription() {
        openAddForm();

        Button saveButton = lookup("#saveButton").queryAs(Button.class);
        Button cancelButton = lookup("#cancelButton").queryAs(Button.class);
        Label titleLabel = lookup("#lblTitle").queryAs(Label.class);

        assertEquals("Save appointment", saveButton.getAccessibleText());
        assertEquals("Cancel", cancelButton.getAccessibleText());
        assertEquals("Appointment form", titleLabel.getAccessibleText());

        assertEquals("Save button", saveButton.getAccessibleRoleDescription());
        assertEquals("Cancel button", cancelButton.getAccessibleRoleDescription());

        assertNotNull(saveButton.getAccessibleHelp());
        assertNotNull(cancelButton.getAccessibleHelp());
        assertNotNull(titleLabel.getAccessibleHelp());

        closeFormSafely();
    }

    @Test
    void invalidFormUpdatesAccessibleHelpWithErrorMessage() {
        openAddForm();

        fireSave();

        TextField nameField = lookup("#fieldName").queryAs(TextField.class);
        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);
        DatePicker dateField = lookup("#fieldDate").queryAs(DatePicker.class);

        assertTrue(nameField.getAccessibleHelp().toLowerCase().contains("error"));
        assertTrue(phoneField.getAccessibleHelp().toLowerCase().contains("error"));
        assertTrue(dateField.getAccessibleHelp().toLowerCase().contains("error"));

        closeOkDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void validationDialog_hasAccessibleMetadata() {
        openAddForm();

        fireSave();

        Label errName = lookup("#lblErrName").queryAs(Label.class);
        assertFalse(errName.getText().isBlank());

        DialogPane pane = findDialogPaneIfPresent();
        if (pane != null) {
            assertEquals("Validation error dialog", pane.getAccessibleText());
            assertNotNull(pane.getAccessibleHelp());
            assertFalse(pane.getAccessibleHelp().isBlank());
        }

        closeOkDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void warningDialog_hasAccessibleMetadata() {
        ensureSidebarOpenAndReady();
        clickOn("#btnEdit");
        sleep(300);

        DialogPane pane = findDialogPaneIfPresent();
        assertNotNull(pane);
        assertEquals("Warning dialog", pane.getAccessibleText());
        assertNotNull(pane.getAccessibleHelp());
        assertFalse(pane.getAccessibleHelp().isBlank());

        closeOkDialogIfPresent();
    }

    @Test
    void deleteConfirmationDialog_hasAccessibleMetadata() throws Exception {
        seedAndSelectFirstRow();

        ensureSidebarOpenAndReady();
        clickOn("#btnDelete");
        sleep(300);

        DialogPane pane = findDialogPaneIfPresent();
        assertNotNull(pane);
        assertEquals("Confirmation dialog", pane.getAccessibleText());
        assertNotNull(pane.getAccessibleHelp());
        assertFalse(pane.getAccessibleHelp().isBlank());

        closeConfirmationDialogWithCancelIfPresent();
    }

    @Test
    void selectingRow_updatesTableAccessibleHelp() throws Exception {
        seedAndSelectFirstRow();

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        String help = tableView.getAccessibleHelp();
        assertNotNull(help);
        assertTrue(help.contains("John Carter"));
        assertTrue(help.contains("10:00 AM"));
    }

    @Test
    void detailView_hasAccessibleTextAndHelp() throws Exception {
        openDetailsForFirstRow();

        Parent root = lookup("#root").queryAs(Parent.class);
        Label patientName = lookup("#lblPatientName").queryAs(Label.class);
        Label doctor = lookup("#lblDoctor").queryAs(Label.class);

        assertNotNull(root);
        assertEquals("Emma Stone", patientName.getText());
        assertEquals("Dr. Lee", doctor.getText());

        assertEquals("Patient name", patientName.getAccessibleText());
        assertEquals("Doctor name", doctor.getAccessibleText());
        assertNotNull(root.getAccessibleHelp());
        assertFalse(root.getAccessibleHelp().isBlank());

        closeDetailsSafely();
    }

    @Test
    void keyboardTabMovesFocusOnMainScreen() {
        clickOn("#searchField");
        assertTrue(lookup("#searchField").queryAs(TextField.class).isFocused());

        type(KeyCode.TAB);
        sleep(150);

        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        assertTrue(filterDate.isFocused() || filterStatus.isFocused() || tableView.isFocused());
    }

    @Test
    void enterKeyOnSelectedRowOpensDetailsWindow() throws Exception {
        seedAndSelectFirstRow();

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        clickOn(tableView);
        sleep(200);

        type(KeyCode.ENTER);
        sleep(900);

        assertTrue(lookup("#lblPatientName").tryQuery().isPresent());

        Label patientName = lookup("#lblPatientName").queryAs(Label.class);
        assertEquals("John Carter", patientName.getText());

        closeDetailsSafely();
    }
}