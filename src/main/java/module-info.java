module com.example.healthappointment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;

    // Third-party UI libs from your pom
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    // SQLite JDBC driver
    requires org.xerial.sqlitejdbc;

    opens com.example.healthappointment            to javafx.fxml;
    opens com.example.healthappointment.controller to javafx.fxml;
    opens com.example.healthappointment.model      to javafx.base, javafx.fxml;
    opens com.example.healthappointment.dao        to javafx.fxml;
    opens com.example.healthappointment.util       to javafx.fxml;

    exports com.example.healthappointment;
    exports com.example.healthappointment.controller;
    exports com.example.healthappointment.model;
    exports com.example.healthappointment.dao;
    exports com.example.healthappointment.util;
}