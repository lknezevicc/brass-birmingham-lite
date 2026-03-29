package hr.lknezevic.brassbirmingham.serialization;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that GameState survives a full ObjectOutputStream → ObjectInputStream cycle
 * after multiple game actions have been executed.
 */
class SerializationRoundTripTest {

    @Test
    void fullGameStateSurvivesRoundTrip(@TempDir Path tempDir) throws Exception {
        GameState state = GameStateFactory.newGame("P1", "P2");
        RulesEngine engine = new RulesEngine(state);

        // Play several moves
        for (int i = 0; i < 4; i++) {
            Card card = engine.getState().getCurrentPlayer().getHand().getCards().getFirst();
            engine.submitAction(new LoanAction(card));
        }

        int p1Money = state.getPlayers().get(0).getMoney();
        int p2Money = state.getPlayers().get(1).getMoney();
        int round = state.getCurrentRound();
        int deckSize = state.getDeck().drawPileSize();

        // Serialize
        File file = tempDir.resolve("roundtrip.ser").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
        }

        // Deserialize
        GameState loaded;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            loaded = (GameState) ois.readObject();
        }

        assertThat(loaded.getPlayers().get(0).getMoney()).isEqualTo(p1Money);
        assertThat(loaded.getPlayers().get(1).getMoney()).isEqualTo(p2Money);
        assertThat(loaded.getCurrentRound()).isEqualTo(round);
        assertThat(loaded.getDeck().drawPileSize()).isEqualTo(deckSize);
        assertThat(loaded.getDeck().getSeed()).isEqualTo(state.getDeck().getSeed());

        // Can continue playing from loaded state
        RulesEngine loadedEngine = new RulesEngine(loaded);
        Card card = loaded.getCurrentPlayer().getHand().getCards().getFirst();
        var errors = loadedEngine.submitAction(new LoanAction(card));
        assertThat(errors).isEmpty();
    }
}
