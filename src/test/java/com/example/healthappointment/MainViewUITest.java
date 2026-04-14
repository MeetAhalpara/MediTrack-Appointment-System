package com.example.healthappointment;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainViewUITest extends BaseUiTest {

    @Test
    void mainScreen_loadsCoreControls() {
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);
        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);
        Button hamburgerButton = lookup("#hamburgerButton").queryAs(Button.class);

        assertNotNull(tableView);
        assertNotNull(searchField);
        assertNotNull(filterDate);
        assertNotNull(filterStatus);
        assertNotNull(hamburgerButton);

        assertTrue(tableView.isVisible());
        assertTrue(searchField.isVisible());
        assertTrue(filterDate.isVisible());
        assertTrue(filterStatus.isVisible());
        assertTrue(hamburgerButton.isVisible());
    }

    @Test
    void sidebarToggle_showsActionButtons() {
        clickOn("#hamburgerButton");
        sleep(600);

        Button addButton = lookup("#btnAdd").queryAs(Button.class);
        Button editButton = lookup("#btnEdit").queryAs(Button.class);
        Button deleteButton = lookup("#btnDelete").queryAs(Button.class);
        Button detailsButton = lookup("#btnDetails").queryAs(Button.class);

        assertNotNull(addButton);
        assertNotNull(editButton);
        assertNotNull(deleteButton);
        assertNotNull(detailsButton);

        assertTrue(addButton.isVisible());
        assertTrue(editButton.isVisible());
        assertTrue(deleteButton.isVisible());
        assertTrue(detailsButton.isVisible());
    }

    @Test
    void editWithoutSelection_showsWarningDialog() {
        openSidebarIfHidden(this);

        clickOn("#btnEdit");
        sleep(250);

        assertNotNull(lookup(".dialog-pane").query());
        assertNotNull(lookup("Select appointment first.").queryLabeled());

        clickOn("OK");
    }

    @Test
    void deleteWithoutSelection_showsWarningDialog() {
        openSidebarIfHidden(this);

        clickOn("#btnDelete");
        sleep(250);

        assertNotNull(lookup(".dialog-pane").query());
        assertNotNull(lookup("Select appointment first.").queryLabeled());

        clickOn("OK");
    }

    @Test
    void detailsWithoutSelection_showsWarningDialog() {
        openSidebarIfHidden(this);

        clickOn("#btnDetails");
        sleep(250);

        assertNotNull(lookup(".dialog-pane").query());
        assertNotNull(lookup("Select appointment first.").queryLabeled());

        clickOn("OK");
    }

    @Test
    void mainControls_haveAccessibilityText() {
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
    void actionButtons_haveAccessibilityText() {
        openSidebarIfHidden(this);

        Button addButton = lookup("#btnAdd").queryAs(Button.class);
        Button editButton = lookup("#btnEdit").queryAs(Button.class);
        Button deleteButton = lookup("#btnDelete").queryAs(Button.class);
        Button detailsButton = lookup("#btnDetails").queryAs(Button.class);

        assertEquals("Add appointment", addButton.getAccessibleText());
        assertEquals("Edit appointment", editButton.getAccessibleText());
        assertEquals("Delete appointment", deleteButton.getAccessibleText());
        assertEquals("View appointment details", detailsButton.getAccessibleText());

        assertTrue(addButton.isFocusTraversable());
        assertTrue(editButton.isFocusTraversable());
        assertTrue(deleteButton.isFocusTraversable());
        assertTrue(detailsButton.isFocusTraversable());
    }

    @Test
    void searchField_acceptsTyping() {
        TextField searchField = lookup("#searchField").queryAs(TextField.class);

        clickOn("#searchField");
        write("Smith");

        assertEquals("Smith", searchField.getText());
    }

    @Test
    void filterStatus_containsExpectedOptions() {
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);

        assertTrue(filterStatus.getItems().contains(""));
        assertTrue(filterStatus.getItems().contains("Scheduled"));
        assertTrue(filterStatus.getItems().contains("Completed"));
        assertTrue(filterStatus.getItems().contains("Cancelled"));
    }

    @Test
    void selectedRow_updatesTableAccessibilityHelp() throws Exception {
        seedDefaultAppointment();
        refreshMainTable();
        sleep(500);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("John Carter");
        sleep(200);

        String help = tableView.getAccessibleHelp();
        assertNotNull(help);
        assertTrue(help.contains("John Carter"));
        assertTrue(help.contains("10:00 AM"));
    }
}