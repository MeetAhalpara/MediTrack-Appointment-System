package com.example.healthappointment.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.util.Duration;

public final class GlassButtonAnimator {

    private static final String APPLIED_KEY = "glass.anim.applied";
    private static final String LOOP_KEY = "glass.anim.loop";

    private GlassButtonAnimator() {
    }

    public static void applyToButtons(Parent root) {
        if (root == null) {
            return;
        }

        for (Node node : root.lookupAll(".button")) {
            if (!(node instanceof Button button)) {
                continue;
            }
            if (shouldSkip(button)) {
                continue;
            }
            if (Boolean.TRUE.equals(button.getProperties().get(APPLIED_KEY))) {
                continue;
            }

            if (!button.getStyleClass().contains("glass-animated-button")) {
                button.getStyleClass().add("glass-animated-button");
            }

            button.getProperties().put(APPLIED_KEY, Boolean.TRUE);
            startLoop(button);
            setupHover(button);
        }
    }

    public static void applyToSurface(Node node, String baseClass) {
        if (node == null) {
            return;
        }
        if (!node.getStyleClass().contains(baseClass)) {
            node.getStyleClass().add(baseClass);
        }
        if (!Boolean.TRUE.equals(node.getProperties().get(APPLIED_KEY))) {
            node.getProperties().put(APPLIED_KEY, Boolean.TRUE);
            startLoop(node);
            setupHover(node);
        }
    }

    private static boolean shouldSkip(Button button) {
        return button.getStyleClass().contains("hamburger-btn")
                || button.getStyleClass().contains("nav-btn")
                || button.getStyleClass().contains("get-started-button");
    }

    private static void startLoop(Node node) {
        FadeTransition shimmer = new FadeTransition(Duration.millis(1700), node);
        shimmer.setFromValue(0.985);
        shimmer.setToValue(1.0);
        shimmer.setCycleCount(FadeTransition.INDEFINITE);
        shimmer.setAutoReverse(true);
        shimmer.setInterpolator(Interpolator.SPLINE(0.42, 0.0, 0.2, 1.0));

        ParallelTransition loop = new ParallelTransition(shimmer);
        node.getProperties().put(LOOP_KEY, loop);
        loop.play();
    }

    private static void setupHover(Node node) {
        node.hoverProperty().addListener((obs, wasHover, isHover) -> {
            toggleHoverClass(node, isHover);
        });
    }

    private static void toggleHoverClass(Node node, boolean add) {
        if (add) {
            if (!node.getStyleClass().contains("glass-hover")) {
                node.getStyleClass().add("glass-hover");
            }
            return;
        }
        node.getStyleClass().remove("glass-hover");
    }
}
