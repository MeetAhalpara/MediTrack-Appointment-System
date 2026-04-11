package com.example.healthappointment.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class GetStartedController {

    private static final Duration ENTRY_TOTAL_TIME = Duration.millis(620);

    @FXML private StackPane root;
    @FXML private VBox card;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button getStartedButton;

    private Runnable onGetStarted;
    private boolean navigationLocked;
    private ParallelTransition buttonGlassLoop;

    @FXML
    public void initialize() {
        playEntryAnimation();
        setupButtonHoverAnimation();
        setupResponsiveMode();
    }

    private void setupResponsiveMode() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            applyResponsiveClass(newScene.getWidth());
            newScene.widthProperty().addListener((widthObs, oldWidth, newWidth) ->
                    applyResponsiveClass(newWidth.doubleValue()));
        });
        Platform.runLater(() -> {
            Scene scene = root.getScene();
            if (scene != null) {
                applyResponsiveClass(scene.getWidth());
            }
        });
    }

    private void applyResponsiveClass(double sceneWidth) {
        root.getStyleClass().removeAll("responsive-desktop", "responsive-tablet", "responsive-compact");
        if (sceneWidth < 760) {
            root.getStyleClass().add("responsive-compact");
        } else if (sceneWidth < 1100) {
            root.getStyleClass().add("responsive-tablet");
        } else {
            root.getStyleClass().add("responsive-desktop");
        }
    }

    public void setOnGetStarted(Runnable onGetStarted) {
        this.onGetStarted = onGetStarted;
    }

    @FXML
    private void handleGetStarted() {
        if (navigationLocked) {
            return;
        }

        navigationLocked = true;
        getStartedButton.setDisable(true);
        stopButtonGlassLoop();
        playExitAnimation();
    }

    private void playEntryAnimation() {
        card.setOpacity(1.0);
        card.setTranslateX(0.0);
        titleLabel.setOpacity(1.0);
        subtitleLabel.setOpacity(1.0);
        descriptionLabel.setOpacity(1.0);

        animateNode(getStartedButton, 0, 24, Duration.millis(210), Duration.millis(380), 1.0);

        PauseTransition startPulseDelay = new PauseTransition(ENTRY_TOTAL_TIME.add(Duration.millis(250)));
        startPulseDelay.setOnFinished(evt -> startButtonGlassLoop());
        startPulseDelay.play();
    }

    private void startButtonGlassLoop() {
        stopButtonGlassLoop();

        FadeTransition shimmer = new FadeTransition(Duration.millis(1600), getStartedButton);
        shimmer.setFromValue(0.92);
        shimmer.setToValue(1.0);
        shimmer.setCycleCount(FadeTransition.INDEFINITE);
        shimmer.setAutoReverse(true);
        shimmer.setInterpolator(Interpolator.SPLINE(0.42, 0.0, 0.2, 1.0));

        buttonGlassLoop = new ParallelTransition(shimmer);
        buttonGlassLoop.play();
    }

    private void stopButtonGlassLoop() {
        if (buttonGlassLoop != null) {
            buttonGlassLoop.stop();
            buttonGlassLoop = null;
        }
    }

    private void setupButtonHoverAnimation() {
        getStartedButton.hoverProperty().addListener((obs, wasHover, isHover) -> {
            if (navigationLocked) {
                return;
            }

            toggleStyleClass(getStartedButton, "glass-hover", isHover);
        });
    }

    private void toggleStyleClass(Node node, String styleClass, boolean add) {
        if (add) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
            return;
        }
        node.getStyleClass().remove(styleClass);
    }

    private void playExitAnimation() {
        FadeTransition fadeCard = new FadeTransition(Duration.millis(220), card);
        fadeCard.setFromValue(card.getOpacity());
        fadeCard.setToValue(0);
        fadeCard.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition slideCard = new TranslateTransition(Duration.millis(220), card);
        slideCard.setFromY(card.getTranslateY());
        slideCard.setToY(card.getTranslateY() - 22);
        slideCard.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition exit = new ParallelTransition(fadeCard, slideCard);
        exit.setOnFinished(evt -> {
            if (onGetStarted != null) {
                onGetStarted.run();
            }
        });
        exit.play();
    }

    private void animateNode(Node node,
                             double startOpacity,
                             double startTranslateX,
                             Duration delay,
                             Duration duration,
                             double targetOpacity) {
        node.setOpacity(startOpacity);
        node.setTranslateX(startTranslateX);

        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(startTranslateX);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0));

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(startOpacity);
        fade.setToValue(targetOpacity);
        fade.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition group = new ParallelTransition(slide, fade);
        group.setDelay(delay);
        new SequentialTransition(group).play();
    }
}

