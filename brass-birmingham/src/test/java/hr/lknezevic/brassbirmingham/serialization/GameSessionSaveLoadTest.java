package hr.lknezevic.brassbirmingham.serialization;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.persistence.save.LoadGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GameSessionSaveLoadTest {

    private AppState appState;
    private GameSession session;

    @BeforeEach
    void setup() {
        appState = new AppState();
        session = new GameSession(appState);
    }

    @Test
    void saveAndLoadRestoresPlayableState(@TempDir Path tempDir) {
        File saveFile = tempDir.resolve("session.ser").toFile();
        appState.setSaveFilePath(saveFile.getAbsolutePath());

        session.startLocalGame("Alice", "Bob");

        // Play a loan action
        Card card = session.getLocalEngine().getState().getCurrentPlayer().getHand().getCards().getFirst();
        session.submitAction(new LoanAction(card)).join();

        int moneyBefore = session.getCurrentState().getPlayers().get(0).getMoney();

        session.saveLocalGame();
        assertThat(saveFile).exists();

        // Create a fresh session and load
        AppState newAppState = new AppState();
        GameSession newSession = new GameSession(newAppState);
        newSession.loadLocalGame(saveFile);

        assertThat(newSession.getCurrentState()).isNotNull();
        assertThat(newSession.getCurrentState().getPlayers().get(0).getMoney()).isEqualTo(moneyBefore);
        assertThat(newSession.isMyTurn()).isTrue();
    }

    @Test
    void loadedStateIsPlayable(@TempDir Path tempDir) {
        File saveFile = tempDir.resolve("playable.ser").toFile();
        appState.setSaveFilePath(saveFile.getAbsolutePath());

        session.startLocalGame("X", "Y");
        session.saveLocalGame();

        GameSession newSession = new GameSession(new AppState());
        newSession.loadLocalGame(saveFile);

        // Should be able to play a move after loading
        Card card = newSession.getLocalEngine().getState().getCurrentPlayer().getHand().getCards().getFirst();
        var result = newSession.submitAction(new LoanAction(card)).join();
        assertThat(result.isSuccess()).isTrue();
    }
}
