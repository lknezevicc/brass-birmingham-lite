package hr.lknezevic.brassbirmingham.persistence;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayDocument;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayReader;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayWriter;
import hr.lknezevic.brassbirmingham.reflection.ActionRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end: play moves → write XML → read → replay through engine.
 */
class ReplayIntegrationTest {

    @Test
    void playAndReplayMatchesState(@TempDir Path tempDir) {
        // --- Recording phase ---
        GameState state = GameStateFactory.newGame("Alice", "Bob");
        long seed = state.getDeck().getSeed();
        RulesEngine engine = new RulesEngine(state);
        ReplayWriter writer = new ReplayWriter();
        writer.startGame(List.of("Alice", "Bob"), seed);

        // Play 3 loan moves
        for (int i = 0; i < 3; i++) {
            GameState s = engine.getState();
            int playerIdx = s.getCurrentPlayerIndex();
            Card card = s.getCurrentPlayer().getHand().getCards().getFirst();
            LoanAction action = new LoanAction(card);
            writer.appendMove(s, playerIdx, action);
            List<String> errors = engine.submitAction(action);
            assertThat(errors).isEmpty();
        }

        int moneyAfter3Moves = engine.getState().getPlayers().get(0).getMoney();
        int roundAfter3Moves = engine.getState().getCurrentRound();

        writer.finishGame(-1, new int[]{0, 0});
        File file = tempDir.resolve("integration.xml").toFile();
        writer.writeToFile(file);

        // --- Playback phase ---
        ReplayReader reader = new ReplayReader();
        ReplayDocument doc = reader.readFromFile(file);
        assertThat(doc.getMoves()).hasSize(3);
        assertThat(doc.getDeckSeed()).isEqualTo(seed);

        ActionRegistry registry = new ActionRegistry();
        RulesEngine replayEngine = new RulesEngine(
                GameStateFactory.newGame("Alice", "Bob", doc.getDeckSeed()));

        for (var move : doc.getMoves()) {
            GameAction action = registry.fromXml(move.actionType(), move.params());
            List<String> errors = replayEngine.submitAction(action);
            assertThat(errors).as("Replay move %s failed", move).isEmpty();
        }

        // State should match after replay
        assertThat(replayEngine.getState().getPlayers().get(0).getMoney())
                .isEqualTo(moneyAfter3Moves);
        assertThat(replayEngine.getState().getCurrentRound())
                .isEqualTo(roundAfter3Moves);
    }
}
