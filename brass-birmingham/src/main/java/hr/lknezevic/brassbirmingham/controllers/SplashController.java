package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SplashController {

    private static final Duration INTRO_FADE_DURATION = Duration.seconds(0.8);
    private static final Duration PULSE_DURATION = Duration.seconds(1.2);
    private static final Duration FADE_OUT_DURATION = Duration.seconds(0.6);
    private static final int PULSE_CYCLE_COUNT = 3;
    private static final double INITIAL_LOGO_SCALE = 1.0;
    private static final double LOGO_PULSE_SCALE = 1.25;

    private final SceneManager sceneManager;

    @FXML
    private StackPane root;

    @FXML
    private ImageView logoImage;

    public SplashController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void initialize() {
        root.setOpacity(0.0);
        logoImage.setScaleX(INITIAL_LOGO_SCALE);
        logoImage.setScaleY(INITIAL_LOGO_SCALE);

        Platform.runLater(this::playSplashSequence);
    }

    private void playSplashSequence() {
        FadeTransition intro = fade(root, 0.0, 1.0, INTRO_FADE_DURATION);

        ScaleTransition pulse = new ScaleTransition(PULSE_DURATION, logoImage);
        pulse.setFromX(logoImage.getScaleX());
        pulse.setFromY(logoImage.getScaleY());
        pulse.setToX(LOGO_PULSE_SCALE);
        pulse.setToY(LOGO_PULSE_SCALE);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(PULSE_CYCLE_COUNT);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeOut = fade(root, 1.0, 0.0, FADE_OUT_DURATION);

        intro.setOnFinished(event -> pulse.play());
        pulse.setOnFinished(event -> fadeOut.play());
        fadeOut.setOnFinished(event -> sceneManager.switchTo(SceneType.LOBBY));

        intro.play();
    }

    private FadeTransition fade(Node node, double from, double to, Duration duration) {
        FadeTransition transition = new FadeTransition(duration, node);
        transition.setFromValue(from);
        transition.setToValue(to);

        return transition;
    }
}
