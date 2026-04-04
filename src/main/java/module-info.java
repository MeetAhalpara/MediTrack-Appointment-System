module com.example.healthappointment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.healthappointment to javafx.fxml;
    opens com.example.healthappointment.controller to javafx.fxml;
    opens com.example.healthappointment.model to javafx.base, javafx.fxml;
    opens com.example.healthappointment.dao to javafx.fxml;
    opens com.example.healthappointment.util to javafx.fxml;

    exports com.example.healthappointment;
}