package com.example.healthappointment;

import com.example.healthappointment.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main — JavaFX application entry point.
 *
 * Loads the Dashboard view (main-view.fxml) and ensures the database
 * connection is closed cleanly on exit.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        // Apply global stylesheet
        scene.getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Health Appointment Manager");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    /** Close the database connection gracefully when the app exits. */
    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch();
    }
}