package com.example.healthappointment;

import com.example.healthappointment.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseConnection.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/healthappointment/main-view.fxml"));

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("Health Appointment Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}