package com.example.healthappointment;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentFormUITest extends BaseUiTest {

    private void ensureSidebarOpen() {
        VBox sidebar = lookup("#sidebar").queryAs(VBox.class);
        if (!sidebar.isVisible()) {
            clickOn("#hamburgerButton");
            sleep(700);
        }
    }

    private void openAddForm() {
        ensureSidebarOpen();
        clickOn("#btnAdd");
        waitForFormToBeReady();
    }

    private void openEditFormForFirstRow() throws Exception {
        seedDefaultAppointment();
        refreshMainTable();
        sleep(500);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        assertFalse(tableView.getItems().isEmpty());

        interact(() -> tableView.getSelectionModel().select(0));
        sleep(250);

        ensureSidebarOpen();
        clickOn("#btnEdit");
        waitForFormToBeReady();
    }

    private void waitForFormToBeReady() {
        sleep(700);
        assertTrue(lookup("#fieldName").tryQuery().isPresent(), "Form fieldName should be present");
        assertTrue(lookup("#saveButton").tryQuery().isPresent(), "saveButton should be present");
        assertTrue(lookup("#cancelButton").tryQuery().isPresent(), "cancelButton should be present");
    }

    private void closeFormSafely() {
        if (lookup("#cancelButton").tryQuery().isPresent()) {
            interact(() -> {
                Button cancelButton = lookup("#cancelButton").queryAs(Button.class);
                cancelButton.fire();
            });
            sleep(350);
        }
    }

    @Test
    void addButton_opensAddAppointmentForm() {
        openAddForm();

        Label titleLabel = lookup("#lblTitle").queryAs(Label.class);
        Button saveButton = lookup("#saveButton").queryAs(Button.class);
        Button cancelButton = lookup("#cancelButton").queryAs(Button.class);

        assertNotNull(titleLabel);
        assertEquals("Add Appointment", titleLabel.getText());

        assertNotNull(saveButton);
        assertNotNull(cancelButton);

        closeFormSafely();
    }

    @Test
    void cancelButton_closesAddForm() {
        openAddForm();

        assertNotNull(lookup("#fieldName").queryAs(TextField.class));

        closeFormSafely();

        assertTrue(lookup("#fieldName").tryQuery().isEmpty());
    }

    @Test
    void phoneField_formatsInputAutomatically() {
        openAddForm();

        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);

        clickOn("#fieldPhone");
        write("6135551234");
        sleep(200);

        assertEquals("(613) 555-1234", phoneField.getText());

        closeFormSafely();
    }

    @Test
    void addForm_validAppointment_savesAndAppearsInTable() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");

        DatePicker datePicker = lookup("#fieldDate").queryAs(DatePicker.class);
        interact(() -> datePicker.setValue(LocalDate.now().plusDays(3)));

        clickOn("#fieldTime");
        clickOn("10:00 AM");

        clickOn("#fieldDoctor");
        clickOn("Dr. Smith");

        clickOn("#fieldReason").write("Routine Checkup");

        clickOn("#fieldStatus");
        clickOn("Scheduled");

        interact(() -> lookup("#saveButton").queryAs(Button.class).fire());
        sleep(900);

        refreshMainTable();
        sleep(350);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        assertNotNull(tableView);
        assertFalse(tableView.getItems().isEmpty());
        assertTrue(
                lookup("Alice Brown").tryQuery().isPresent(),
                "Saved appointment should appear in table"
        );
    }

    @Test
    void editButton_opensEditFormWithExistingValues() throws Exception {
        openEditFormForFirstRow();

        Label titleLabel = lookup("#lblTitle").queryAs(Label.class);
        TextField nameField = lookup("#fieldName").queryAs(TextField.class);
        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);
        TextField reasonField = lookup("#fieldReason").queryAs(TextField.class);

        assertEquals("Edit Appointment", titleLabel.getText());
        assertEquals("John Carter", nameField.getText());
        assertEquals("(613) 555-1234", phoneField.getText());
        assertEquals("General Checkup", reasonField.getText());

        closeFormSafely();
    }

    @Test
    void editForm_saveUpdatesAppointmentInTable() throws Exception {
        openEditFormForFirstRow();

        TextField reasonField = lookup("#fieldReason").queryAs(TextField.class);
        interact(() -> reasonField.setText("Updated Follow-up"));

        interact(() -> lookup("#saveButton").queryAs(Button.class).fire());
        sleep(900);

        refreshMainTable();
        sleep(350);

        assertTrue(
                lookup("Updated Follow-up").tryQuery().isPresent(),
                "Updated reason should appear in table"
        );
    }

    @Test
    void addForm_controlsHaveAccessibilityText() {
        openAddForm();

        TextField nameField = lookup("#fieldName").queryAs(TextField.class);
        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);
        TextField reasonField = lookup("#fieldReason").queryAs(TextField.class);
        Button saveButton = lookup("#saveButton").queryAs(Button.class);
        Button cancelButton = lookup("#cancelButton").queryAs(Button.class);
        Label titleLabel = lookup("#lblTitle").queryAs(Label.class);

        assertEquals("Patient name", nameField.getAccessibleText());
        assertEquals("Phone number", phoneField.getAccessibleText());
        assertEquals("Reason for visit", reasonField.getAccessibleText());
        assertEquals("Save appointment", saveButton.getAccessibleText());
        assertEquals("Cancel", cancelButton.getAccessibleText());
        assertEquals("Appointment form", titleLabel.getAccessibleText());

        closeFormSafely();
    }
}