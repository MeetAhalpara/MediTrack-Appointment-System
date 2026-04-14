package com.example.healthappointment;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentFilterUITest extends BaseUiTest {

    @Test
    void searchByPatientName_filtersTable() throws Exception {
        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                LocalDate.now().plusDays(3),
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        sleep(300);

        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("#searchField");
        write("John");
        sleep(400);

        assertEquals("John", searchField.getText());
        assertEquals(1, tableView.getItems().size());
        assertNotNull(lookup("John Carter").queryLabeled());
    }

    @Test
    void searchByDoctor_filtersTable() throws Exception {
        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                LocalDate.now().plusDays(3),
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        sleep(300);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("#searchField");
        write("Smith");
        sleep(400);

        assertEquals(1, tableView.getItems().size());
        assertNotNull(lookup("John Carter").queryLabeled());
    }

    @Test
    void searchWithNoMatch_showsEmptyTable() throws Exception {
        seedDefaultAppointment();
        sleep(300);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("#searchField");
        write("ZZZNOMATCH999");
        sleep(400);

        assertEquals(0, tableView.getItems().size());
    }

    @Test
    void filterByStatus_showsOnlyScheduled() throws Exception {
        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                LocalDate.now().plusDays(3),
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        sleep(300);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("#filterStatus");
        clickOn("Scheduled");
        sleep(400);

        assertEquals(1, tableView.getItems().size());
        assertNotNull(lookup("John Carter").queryLabeled());
    }

    @Test
    void filterByDate_showsOnlyMatchingDate() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(5);

        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                targetDate,
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                targetDate.plusDays(1),
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        sleep(300);

        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        interact(() -> filterDate.setValue(targetDate));
        clickOn("#searchField");
        type(javafx.scene.input.KeyCode.ENTER);
        sleep(400);

        assertEquals(1, tableView.getItems().size());
        assertNotNull(lookup("John Carter").queryLabeled());
    }

    @Test
    void combinedDateAndStatusFilter_showsExactMatch() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(6);

        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                targetDate,
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                targetDate,
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        seedAppointment(
                "Mike Stone",
                "(613) 555-2222",
                targetDate.plusDays(1),
                "01:00 PM",
                "Dr. Patel",
                "Follow-up",
                "Scheduled"
        );

        sleep(300);

        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        interact(() -> filterDate.setValue(targetDate));
        clickOn("#filterStatus");
        clickOn("Scheduled");
        sleep(400);

        assertEquals(1, tableView.getItems().size());
        assertNotNull(lookup("John Carter").queryLabeled());
    }

    @Test
    void clearFilter_restoresFullTable() throws Exception {
        seedAppointment(
                "John Carter",
                "(613) 555-1234",
                LocalDate.now().plusDays(2),
                "10:00 AM",
                "Dr. Smith",
                "Checkup",
                "Scheduled"
        );

        seedAppointment(
                "Alice Brown",
                "(613) 555-1111",
                LocalDate.now().plusDays(3),
                "11:00 AM",
                "Dr. Lee",
                "Consultation",
                "Completed"
        );

        sleep(300);

        TableView<?> tableView = lookup("#tableView").queryAs(TableView.class);

        clickOn("#searchField");
        write("John");
        sleep(400);

        assertEquals(1, tableView.getItems().size());

        clickOn("#clearFilterButton");
        sleep(400);

        assertEquals(2, tableView.getItems().size());
    }

    @Test
    void filterControls_haveAccessibilityText() {
        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        DatePicker filterDate = lookup("#filterDate").queryAs(DatePicker.class);
        ComboBox<?> filterStatus = lookup("#filterStatus").queryAs(ComboBox.class);

        assertEquals("Search appointments", searchField.getAccessibleText());
        assertEquals("Filter by date", filterDate.getAccessibleText());
        assertEquals("Filter by status", filterStatus.getAccessibleText());

        assertNotNull(searchField.getAccessibleHelp());
        assertNotNull(filterDate.getAccessibleHelp());
        assertNotNull(filterStatus.getAccessibleHelp());
    }
}