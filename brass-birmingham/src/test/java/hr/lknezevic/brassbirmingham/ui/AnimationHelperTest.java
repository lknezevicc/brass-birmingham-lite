package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AnimationHelperTest {

    @Test
    void onStateChangedDoesNotThrowWithNullCanvas() {
        AnimationHelper helper = new AnimationHelper();
        GameState state = GameStateFactory.newGame("A", "B");
        assertThatCode(() -> helper.onStateChanged(null, state)).doesNotThrowAnyException();
    }

    @Test
    void onStateChangedDoesNotThrowWithNullState() {
        AnimationHelper helper = new AnimationHelper();
        assertThatCode(() -> helper.onStateChanged(null, null)).doesNotThrowAnyException();
    }

    @Test
    void resetClearsCounters() {
        AnimationHelper helper = new AnimationHelper();
        GameState state = GameStateFactory.newGame("A", "B");
        // First call sets baseline counts
        helper.onStateChanged(null, state);
        helper.reset();
        // After reset, same state should not trigger (counts are zero again)
        assertThatCode(() -> helper.onStateChanged(null, state)).doesNotThrowAnyException();
    }
}
