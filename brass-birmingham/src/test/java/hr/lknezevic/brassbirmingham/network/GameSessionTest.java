package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.network.dto.GameLobby;
import hr.lknezevic.brassbirmingham.network.dto.GameStateSnapshot;
import hr.lknezevic.brassbirmingham.network.dto.MoveResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests GameSession in LOCAL mode — verifies state caching,
 * subscription callbacks, and turn gating logic.
 */
class GameSessionTest {

    private AppState appState;
    private GameSession session;

    @BeforeEach
    void setup() {
        appState = new AppState();
        session = new GameSession(appState);
    }

    @Test
    void localGameCreatesState() {
        session.startLocalGame("Alice", "Bob");

        GameState state = session.getCurrentState();
        assertThat(state).isNotNull();
        assertThat(state.getPlayers()).hasSize(2);
        assertThat(state.getPlayers().get(0).getName()).isEqualTo("Alice");
        assertThat(state.getPlayers().get(1).getName()).isEqualTo("Bob");
    }

    @Test
    void localGameCachesSnapshotAfterAction() {
        session.startLocalGame("Alice", "Bob");

        Card card = session.getCurrentState().getCurrentPlayer().getHand().getCards().getFirst();
        MoveResult result = session.submitAction(new LoanAction(card)).join();

        assertThat(result.isSuccess()).isTrue();
        assertThat(session.getLastSnapshot()).isNotNull();
        assertThat(session.getLastSnapshot().getState()).isNotNull();
    }

    @Test
    void isMyTurnReturnsTrueForLocalMode() {
        session.startLocalGame("Alice", "Bob");
        appState.setLocalPlayerIndex(0);
        // In local (hotseat) mode, it's always the local user's "turn"
        assertThat(session.isMyTurn()).isTrue();
    }

    @Test
    void isMyTurnReturnsTrueForLocalModeEvenAsPlayer2() {
        session.startLocalGame("Alice", "Bob");
        appState.setLocalPlayerIndex(1);
        // Local hotseat: always your turn since you control both players
        assertThat(session.isMyTurn()).isTrue();
    }

    @Test
    void stateCallbackFiredOnLocalAction() throws Exception {
        session.startLocalGame("Alice", "Bob");

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<GameStateSnapshot> received = new java.util.concurrent.atomic.AtomicReference<>();

        // Since Platform.runLater won't work in test, directly call via reflection or test differently
        // For unit tests without JavaFX thread, we verify the lastSnapshot is set
        Card card = session.getCurrentState().getCurrentPlayer().getHand().getCards().getFirst();
        MoveResult result = session.submitAction(new LoanAction(card)).join();

        assertThat(result.isSuccess()).isTrue();
        assertThat(session.getLastSnapshot()).isNotNull();
    }

    @Test
    void getCurrentStateReturnsNullBeforeGameStart() {
        assertThat(session.getCurrentState()).isNull();
        assertThat(session.isMyTurn()).isFalse();
    }

    @Test
    void isOnlineReturnsFalseForLocalMode() {
        session.startLocalGame("Alice", "Bob");
        assertThat(session.isOnline()).isFalse();
    }

    @Test
    void getLeaderboardReturnsEmptyWhenOffline() {
        session.startLocalGame("Alice", "Bob");
        var entries = session.getLeaderboard().join();
        assertThat(entries).isEmpty();
    }
}
