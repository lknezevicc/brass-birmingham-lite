package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.game.PlacedIndustry;
import hr.lknezevic.brassbirmingham.model.game.PlacedLink;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.ColorAdjust;
import javafx.util.Duration;

import java.util.List;

public final class AnimationHelper {

    private int lastIndustryCount = 0;
    private int lastLinkCount = 0;
    private int lastFlippedCount = 0;

    public void onStateChanged(Canvas canvas, GameState state) {
        if (canvas == null || state == null) return;

        int currentIndustries = state.getBoard().getPlacedIndustries().size();
        int currentLinks = state.getBoard().getPlacedLinks().size();
        int currentFlipped = countFlipped(state.getBoard().getPlacedIndustries());

        if (currentIndustries > lastIndustryCount) {
            playPlacementPulse(canvas);
        } else if (currentLinks > lastLinkCount) {
            playLinkFade(canvas);
        } else if (currentFlipped > lastFlippedCount) {
            playFlipPulse(canvas);
        }

        lastIndustryCount = currentIndustries;
        lastLinkCount = currentLinks;
        lastFlippedCount = currentFlipped;
    }

    public void reset() {
        lastIndustryCount = 0;
        lastLinkCount = 0;
        lastFlippedCount = 0;
    }

    private void playPlacementPulse(Canvas canvas) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), canvas);
        st.setFromX(0.97);
        st.setFromY(0.97);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private void playLinkFade(Canvas canvas) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), canvas);
        ft.setFromValue(0.7);
        ft.setToValue(1.0);
        ft.play();
    }

    private void playFlipPulse(Canvas canvas) {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(0.3);
        canvas.setEffect(adjust);

        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> canvas.setEffect(null));
        pause.play();
    }

    private int countFlipped(List<PlacedIndustry> industries) {
        return (int) industries.stream().filter(PlacedIndustry::isFlipped).count();
    }
}
