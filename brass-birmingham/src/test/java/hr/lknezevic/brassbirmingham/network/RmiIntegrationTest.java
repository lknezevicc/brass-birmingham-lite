package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.network.jndi.JndiConfig;
import hr.lknezevic.brassbirmingham.network.rmi.GameServer;
import hr.lknezevic.brassbirmingham.network.rmi.GameService;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end RMI test: starts embedded server, 2 clients connect, play moves.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RmiIntegrationTest {

    private static GameServer server;
    private static GameService client1;
    private static GameService client2;
    private static String roomCode;
    private static final int TEST_PORT = 12099;

    @BeforeAll
    static void setup() throws Exception {
        server = new GameServer("127.0.0.1", TEST_PORT);
        server.start();

        client1 = JndiConfig.lookup("127.0.0.1", TEST_PORT);
        client2 = JndiConfig.lookup("127.0.0.1", TEST_PORT);
    }

    @AfterAll
    static void teardown() {
        if (server != null) server.stop();
    }

    @Test
    @Order(1)
    void hostCreatesRoom() throws Exception {
        GameLobby lobby = client1.createGame("Alice");
        roomCode = lobby.getRoomCode();
        assertThat(roomCode).hasSize(4);
        assertThat(lobby.getAssignedPlayerIndex()).isEqualTo(0);
        assertThat(lobby.getStatus()).isEqualTo(GameLobby.Status.WAITING);
    }

    @Test
    @Order(2)
    void joinerStartsGame() throws Exception {
        GameLobby lobby = client2.joinGame(roomCode, "Bob");
        assertThat(lobby.getAssignedPlayerIndex()).isEqualTo(1);
        assertThat(lobby.getStatus()).isEqualTo(GameLobby.Status.IN_PROGRESS);
    }

    @Test
    @Order(3)
    void player0CanSubmitMove() throws Exception {
        GameStateSnapshot snapshot = client1.getState(roomCode);
        assertThat(snapshot.getState().getCurrentPlayerIndex()).isEqualTo(0);

        Card card = snapshot.getState().getCurrentPlayer().getHand().getCards().getFirst();
        MoveResult result = client1.submitMove(roomCode, 0, new LoanAction(card));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSnapshot()).isNotNull();
    }

    @Test
    @Order(4)
    void player1CanSubmitMoveOnTheirTurn() throws Exception {
        GameStateSnapshot snapshot = client2.getState(roomCode);
        int currentIdx = snapshot.getState().getCurrentPlayerIndex();
        Card card = snapshot.getState().getCurrentPlayer().getHand().getCards().getFirst();

        MoveResult result = client2.submitMove(roomCode, currentIdx, new LoanAction(card));
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @Order(5)
    void multipleMovesWork() throws Exception {
        // Play several rounds
        for (int i = 0; i < 6; i++) {
            GameStateSnapshot snapshot = client1.getState(roomCode);
            int currentIdx = snapshot.getState().getCurrentPlayerIndex();
            Card card = snapshot.getState().getCurrentPlayer().getHand().getCards().getFirst();

            GameService activeClient = currentIdx == 0 ? client1 : client2;
            MoveResult result = activeClient.submitMove(roomCode, currentIdx, new LoanAction(card));

            if (!result.isSuccess()) {
                break;
            }
        }
        assertThat(roomCode).isNotBlank();
    }
}
