package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.network.client.RemoteGameClient;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.network.rmi.GameServer;
import hr.lknezevic.brassbirmingham.network.rmi.GameUpdateListener;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that RMI listener callbacks (state updates,
 * chat messages, game start) are delivered correctly between two clients.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ListenerCallbackIntegrationTest {

    private static GameServer server;
    private static RemoteGameClient client1;
    private static RemoteGameClient client2;
    private static final int TEST_PORT = 13099;

    @BeforeAll
    static void setup() throws Exception {
        server = new GameServer("127.0.0.1", TEST_PORT);
        server.start();

        client1 = new RemoteGameClient();
        client1.connect("127.0.0.1", TEST_PORT);

        client2 = new RemoteGameClient();
        client2.connect("127.0.0.1", TEST_PORT);
    }

    @AfterAll
    static void teardown() {
        if (server != null) server.stop();
    }

    @Test
    @Order(1)
    void hostReceivesGameStartedWhenPlayer2Joins() throws Exception {
        CountDownLatch startedLatch = new CountDownLatch(1);
        AtomicReference<GameLobby> startedLobby = new AtomicReference<>();

        GameUpdateListener listener1 = createTestListener(null, null, lobby -> {
            startedLobby.set(lobby);
            startedLatch.countDown();
        }, null);

        GameLobby hostLobby = client1.createGame("Host");
        String roomCode = hostLobby.getRoomCode();
        client1.subscribe(roomCode, listener1);

        client2.joinGame(roomCode, "Joiner");

        boolean received = startedLatch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(startedLobby.get()).isNotNull();
        assertThat(startedLobby.get().getStatus()).isEqualTo(GameLobby.Status.IN_PROGRESS);
    }

    @Test
    @Order(2)
    void opponentReceivesStateUpdateAfterMove() throws Exception {
        GameLobby hostLobby = client1.createGame("Alice");
        String roomCode = hostLobby.getRoomCode();

        CountDownLatch updateLatch = new CountDownLatch(1);
        AtomicReference<GameStateSnapshot> receivedState = new AtomicReference<>();

        GameUpdateListener listener2 = createTestListener(s -> {
            receivedState.set(s);
            updateLatch.countDown();
        }, null, null, null);

        client2.joinGame(roomCode, "Bob");
        client2.subscribe(roomCode, listener2);

        GameStateSnapshot snap = client1.getState(roomCode);
        Card card = snap.getState().getCurrentPlayer().getHand().getCards().getFirst();
        client1.submitMove(roomCode, 0, new LoanAction(card));

        boolean received = updateLatch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(receivedState.get()).isNotNull();
    }

    @Test
    @Order(3)
    void chatMessageDeliveredToOpponent() throws Exception {
        GameLobby hostLobby = client1.createGame("ChatHost");
        String roomCode = hostLobby.getRoomCode();

        CountDownLatch chatLatch = new CountDownLatch(1);
        AtomicReference<ChatMessage> receivedMsg = new AtomicReference<>();

        GameUpdateListener listener2 = createTestListener(null, m -> {
            receivedMsg.set(m);
            chatLatch.countDown();
        }, null, null);

        client2.joinGame(roomCode, "ChatJoiner");
        client2.subscribe(roomCode, listener2);

        client1.sendChat(roomCode, 0, "Hello from host!");

        boolean received = chatLatch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(receivedMsg.get().getText()).isEqualTo("Hello from host!");
        assertThat(receivedMsg.get().getSender()).isEqualTo("ChatHost");
    }

    @FunctionalInterface
    private interface Callback<T> {
        void accept(T value) throws RemoteException;
    }

    private static GameUpdateListener createTestListener(
            Callback<GameStateSnapshot> onState,
            Callback<ChatMessage> onChat,
            Callback<GameLobby> onStarted,
            Callback<MoveResult> onOver) throws RemoteException {

        return (GameUpdateListener) UnicastRemoteObject.exportObject(new GameUpdateListener() {
            @Override
            public void onStateUpdated(GameStateSnapshot snapshot) throws RemoteException {
                if (onState != null) onState.accept(snapshot);
            }
            @Override
            public void onChatMessage(ChatMessage message) throws RemoteException {
                if (onChat != null) onChat.accept(message);
            }
            @Override
            public void onGameStarted(GameLobby lobby) throws RemoteException {
                if (onStarted != null) onStarted.accept(lobby);
            }
            @Override
            public void onGameOver(MoveResult result) throws RemoteException {
                if (onOver != null) onOver.accept(result);
            }
        }, 0);
    }
}
