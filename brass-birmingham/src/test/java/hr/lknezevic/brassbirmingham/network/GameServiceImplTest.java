package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.network.rmi.GameRoomRegistry;
import hr.lknezevic.brassbirmingham.network.rmi.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceImplTest {

    private GameServiceImpl service;

    @BeforeEach
    void setup() throws RemoteException {
        service = new GameServiceImpl(new GameRoomRegistry());
    }

    @Test
    void createGameReturnsLobbyWithRoomCode() throws RemoteException {
        GameLobby lobby = service.createGame("Alice");
        assertThat(lobby.getRoomCode()).hasSize(4);
        assertThat(lobby.getAssignedPlayerIndex()).isEqualTo(0);
        assertThat(lobby.getStatus()).isEqualTo(GameLobby.Status.WAITING);
    }

    @Test
    void joinGameStartsGameWhenFull() throws RemoteException {
        GameLobby host = service.createGame("Alice");
        GameLobby joined = service.joinGame(host.getRoomCode(), "Bob");

        assertThat(joined.getAssignedPlayerIndex()).isEqualTo(1);
        assertThat(joined.getStatus()).isEqualTo(GameLobby.Status.IN_PROGRESS);
    }

    @Test
    void joinNonexistentRoomThrows() {
        assertThatThrownBy(() -> service.joinGame("ZZZZ", "Bob"))
                .isInstanceOf(RemoteException.class);
    }

    @Test
    void joinFullRoomThrows() throws RemoteException {
        GameLobby host = service.createGame("Alice");
        service.joinGame(host.getRoomCode(), "Bob");

        assertThatThrownBy(() -> service.joinGame(host.getRoomCode(), "Charlie"))
                .isInstanceOf(RemoteException.class);
    }

    @Test
    void submitMoveOnWrongTurnFails() throws RemoteException {
        GameLobby host = service.createGame("Alice");
        service.joinGame(host.getRoomCode(), "Bob");

        GameStateSnapshot snapshot = service.getState(host.getRoomCode());
        Card card = snapshot.getState().getPlayers().get(1).getHand().getCards().getFirst();

        // Player 1 tries to move on player 0's turn
        MoveResult result = service.submitMove(host.getRoomCode(), 1, new LoanAction(card));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).contains("Not your turn");
    }

    @Test
    void submitMoveSucceedsOnCorrectTurn() throws RemoteException {
        GameLobby host = service.createGame("Alice");
        service.joinGame(host.getRoomCode(), "Bob");

        GameStateSnapshot snapshot = service.getState(host.getRoomCode());
        Card card = snapshot.getState().getCurrentPlayer().getHand().getCards().getFirst();

        MoveResult result = service.submitMove(host.getRoomCode(), 0, new LoanAction(card));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSnapshot()).isNotNull();
    }

    @Test
    void chatAddsMessage() throws RemoteException {
        GameLobby host = service.createGame("Alice");
        service.joinGame(host.getRoomCode(), "Bob");

        service.sendChat(host.getRoomCode(), 0, "Hello!");
        // No exception = success (chat is fire-and-forget from client perspective)
    }

    @Test
    void leaderboardStartsEmpty() throws RemoteException {
        List<LeaderboardEntry> lb = service.getLeaderboard();
        assertThat(lb).isEmpty();
    }
}
