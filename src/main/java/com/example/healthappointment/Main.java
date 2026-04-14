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
                getClass().getResource("/com/example/healthappointment/get-started-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Health Appointment Manager");
        stage.setScene(scene);
        stage.show();

        // Set up navigation from GetStarted to Main Dashboard
        Object controller = loader.getController();
        if (controller instanceof com.example.healthappointment.controller.GetStartedController) {
            ((com.example.healthappointment.controller.GetStartedController) controller).setOnGetStarted(() -> {
                try {
                    FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/com/example/healthappointment/main-view.fxml"));
                    Scene mainScene = new Scene(mainLoader.load(), 900, 600);
                    stage.setScene(mainScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        launch();
    }
}