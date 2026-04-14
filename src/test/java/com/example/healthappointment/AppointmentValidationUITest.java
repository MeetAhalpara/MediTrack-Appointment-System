package com.example.healthappointment;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentValidationUITest extends BaseUiTest {

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

    private void waitForFormToBeReady() {
        sleep(700);
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
        sleep(350);
        return lookup(".dialog-pane").tryQuery().map(node -> (DialogPane) node).orElse(null);
    }

    private void closeDialogIfPresent() {
        if (lookup("OK").tryQuery().isPresent()) {
            clickOn("OK");
            sleep(250);
        }
    }

    @Test
    void saveWithAllFieldsEmpty_showsValidationDialogAndInlineErrors() {
        openAddForm();

        fireSave();

        Label errName = lookup("#lblErrName").queryAs(Label.class);
        Label errPhone = lookup("#lblErrPhone").queryAs(Label.class);
        Label errDate = lookup("#lblErrDate").queryAs(Label.class);
        Label errTime = lookup("#lblErrTime").queryAs(Label.class);
        Label errDoctor = lookup("#lblErrDoctor").queryAs(Label.class);
        Label errReason = lookup("#lblErrReason").queryAs(Label.class);

        assertFalse(errName.getText().isBlank());
        assertFalse(errPhone.getText().isBlank());
        assertFalse(errDate.getText().isBlank());
        assertFalse(errTime.getText().isBlank());
        assertFalse(errDoctor.getText().isBlank());
        assertFalse(errReason.getText().isBlank());

        DialogPane pane = findDialogPaneIfPresent();
        if (pane != null) {
            assertEquals("Validation error dialog", pane.getAccessibleText());
            assertNotNull(pane.getAccessibleHelp());
            assertFalse(pane.getAccessibleHelp().isBlank());
        }

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void emptyName_showsInlineNameError() {
        openAddForm();

        clickOn("#fieldPhone").write("6135551111");
        fireSave();

        Label errName = lookup("#lblErrName").queryAs(Label.class);
        TextField nameField = lookup("#fieldName").queryAs(TextField.class);

        assertFalse(errName.getText().isBlank());
        assertTrue(nameField.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void invalidPhone_showsInlinePhoneError() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("123");
        fireSave();

        Label errPhone = lookup("#lblErrPhone").queryAs(Label.class);
        TextField phoneField = lookup("#fieldPhone").queryAs(TextField.class);

        assertFalse(errPhone.getText().isBlank());
        assertTrue(phoneField.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void missingDate_showsInlineDateError() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");
        fireSave();

        Label errDate = lookup("#lblErrDate").queryAs(Label.class);
        DatePicker datePicker = lookup("#fieldDate").queryAs(DatePicker.class);

        assertFalse(errDate.getText().isBlank());
        assertTrue(datePicker.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void missingTime_showsInlineTimeError() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");
        interact(() -> lookup("#fieldDate").queryAs(DatePicker.class)
                .setValue(LocalDate.now().plusDays(2)));

        fireSave();

        Label errTime = lookup("#lblErrTime").queryAs(Label.class);
        ComboBox<?> timeBox = lookup("#fieldTime").queryAs(ComboBox.class);

        assertFalse(errTime.getText().isBlank());
        assertTrue(timeBox.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void missingDoctor_showsInlineDoctorError() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");
        interact(() -> lookup("#fieldDate").queryAs(DatePicker.class)
                .setValue(LocalDate.now().plusDays(2)));
        clickOn("#fieldTime");
        clickOn("10:00 AM");

        fireSave();

        Label errDoctor = lookup("#lblErrDoctor").queryAs(Label.class);
        ComboBox<?> doctorBox = lookup("#fieldDoctor").queryAs(ComboBox.class);

        assertFalse(errDoctor.getText().isBlank());
        assertTrue(doctorBox.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void missingReason_showsInlineReasonError() {
        openAddForm();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");
        interact(() -> lookup("#fieldDate").queryAs(DatePicker.class)
                .setValue(LocalDate.now().plusDays(2)));
        clickOn("#fieldTime");
        clickOn("10:00 AM");
        clickOn("#fieldDoctor");
        clickOn("Dr. Smith");

        fireSave();

        Label errReason = lookup("#lblErrReason").queryAs(Label.class);
        TextField reasonField = lookup("#fieldReason").queryAs(TextField.class);

        assertFalse(errReason.getText().isBlank());
        assertTrue(reasonField.getAccessibleHelp().toLowerCase().contains("error"));

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void validationDialog_hasAccessibilityMetadata() {
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

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void invalidSave_focusesFirstInvalidField_nameFirst() {
        openAddForm();

        fireSave();

        TextField nameField = lookup("#fieldName").queryAs(TextField.class);
        assertTrue(nameField.isFocused());

        closeDialogIfPresent();
        closeFormSafely();
    }

    @Test
    void validData_clearsInlineErrorsAfterSuccessfulSave() {
        openAddForm();

        fireSave();
        closeDialogIfPresent();

        clickOn("#fieldName").write("Alice Brown");
        clickOn("#fieldPhone").write("6135551111");
        interact(() -> lookup("#fieldDate").queryAs(DatePicker.class)
                .setValue(LocalDate.now().plusDays(2)));
        clickOn("#fieldTime");
        clickOn("10:00 AM");
        clickOn("#fieldDoctor");
        clickOn("Dr. Smith");
        clickOn("#fieldReason").write("Routine Checkup");
        clickOn("#fieldStatus");
        clickOn("Scheduled");

        fireSave();
        sleep(700);

        assertTrue(lookup("#fieldName").tryQuery().isEmpty());
        refreshMainTable();
        assertTrue(lookup("Alice Brown").tryQuery().isPresent());
    }
}