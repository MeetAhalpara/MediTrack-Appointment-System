package com.example.healthappointment;

import com.example.healthappointment.controller.GetStartedController;
import com.example.healthappointment.util.DatabaseConnection;
import javafx.application.Application;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class Main extends Application {

    private static final Duration APP_TRANSITION_DURATION = Duration.millis(620);
    private static final Interpolator APP_TRANSITION_EASING = Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0);
    private boolean appTransitionRunning;

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseConnection.initializeDatabase();

        FXMLLoader startLoader = new FXMLLoader(
                getClass().getResource("/com/example/healthappointment/get-started-view.fxml"));
        Parent startRoot = startLoader.load();

        GetStartedController startController = startLoader.getController();
        startController.setOnGetStarted(() -> {
            try {
                showMainView(stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Scene scene = new Scene(startRoot, 900, 600);

        stage.setMinWidth(480);
        stage.setMinHeight(420);
        stage.setTitle("Health Appointment Manager");
        stage.setScene(scene);
        stage.show();
    }

    private void showMainView(Stage stage) throws IOException {
        if (appTransitionRunning || stage.getScene() == null) {
            return;
        }

        appTransitionRunning = true;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/healthappointment/main-view.fxml"));
        Parent mainRoot = loader.load();

        Scene currentScene = stage.getScene();
        Parent startRoot = currentScene.getRoot();

        double width = Math.max(currentScene.getWidth(), stage.getWidth());
        double height = Math.max(currentScene.getHeight(), stage.getHeight());
        if (width <= 0) {
            width = 900;
        }
        if (height <= 0) {
            height = 600;
        }

        mainRoot.setTranslateX(-width);
        mainRoot.setOpacity(0.0);
        startRoot.setTranslateX(0);
        startRoot.setOpacity(1.0);

        StackPane transitionHost = new StackPane(startRoot, mainRoot);
        transitionHost.setPrefSize(width, height);
        currentScene.setRoot(transitionHost);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        APP_TRANSITION_DURATION,
                        new KeyValue(mainRoot.translateXProperty(), 0, APP_TRANSITION_EASING),
                        new KeyValue(mainRoot.opacityProperty(), 1.0, APP_TRANSITION_EASING),
                        new KeyValue(startRoot.translateXProperty(), width, APP_TRANSITION_EASING),
                        new KeyValue(startRoot.opacityProperty(), 0.0, APP_TRANSITION_EASING)
                )
        );

        timeline.setOnFinished(evt -> {
            transitionHost.getChildren().remove(mainRoot);
            currentScene.setRoot(mainRoot);
            mainRoot.setTranslateX(0);
            mainRoot.setOpacity(1.0);
            appTransitionRunning = false;
        });
        timeline.play();
    }

    public static void main(String[] args) {
        launch();
    }
}