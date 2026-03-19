module com.example.healthappointment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.healthappointment to javafx.fxml;
    exports com.example.healthappointment;
    exports com.example.healthappointment.controller;
    opens com.example.healthappointment.controller to javafx.fxml;
}