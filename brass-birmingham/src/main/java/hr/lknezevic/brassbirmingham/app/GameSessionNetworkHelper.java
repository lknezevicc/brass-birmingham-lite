package hr.lknezevic.brassbirmingham.app;

import hr.lknezevic.brassbirmingham.network.client.AsyncGameService;
import hr.lknezevic.brassbirmingham.network.client.RemoteGameClient;
import hr.lknezevic.brassbirmingham.network.client.RemoteUpdateListener;
import hr.lknezevic.brassbirmingham.network.dto.ChatMessage;
import hr.lknezevic.brassbirmingham.network.dto.GameLobby;
import hr.lknezevic.brassbirmingham.network.dto.GameStateSnapshot;
import hr.lknezevic.brassbirmingham.network.dto.MoveResult;
import hr.lknezevic.brassbirmingham.network.rmi.GameServer;
import javafx.application.Platform;
import lombok.Getter;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
public final class GameSessionNetworkHelper {

    private GameServer server;
    private RemoteGameClient client;
    private AsyncGameService asyncService;
    private RemoteUpdateListener updateListener;
    private String roomCode;
    private int playerIndex;

    public CompletableFuture<GameLobby> hostOnlineGame(
            AppState appState, String playerName,
            Consumer<GameStateSnapshot> onStateChange,
            Consumer<ChatMessage> onChatMessage,
            Consumer<GameLobby> onGameStarted,
            Consumer<MoveResult> onGameOver) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String host = appState.getNetworkHost();
                int port = Integer.parseInt(appState.getNetworkPort());

                server = new GameServer(host, port);
                server.start();

                client = new RemoteGameClient();
                client.connect(host, port);
                asyncService = new AsyncGameService(client);

                createListener(onStateChange, onChatMessage, onGameStarted, onGameOver);

                GameLobby lobby = client.createGame(playerName);
                roomCode = lobby.getRoomCode();
                playerIndex = lobby.getAssignedPlayerIndex();

                client.subscribe(roomCode, updateListener);

                Platform.runLater(() -> {
                    appState.setRoomCode(roomCode);
                    appState.setLocalPlayerIndex(playerIndex);
                    appState.setConnectionStatus("Hosting");
                });

                return lobby;
            } catch (Exception e) {
                throw new RuntimeException("Failed to host game", e);
            }
        });
    }

    public CompletableFuture<GameLobby> joinOnlineGame(
            AppState appState,
            String host, int port, String joinRoomCode, String playerName,
            Consumer<GameStateSnapshot> onStateChange,
            Consumer<ChatMessage> onChatMessage,
            Consumer<GameLobby> onGameStarted,
            Consumer<MoveResult> onGameOver) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                client = new RemoteGameClient();
                client.connect(host, port);
                asyncService = new AsyncGameService(client);

                createListener(onStateChange, onChatMessage, onGameStarted, onGameOver);

                GameLobby lobby = client.joinGame(joinRoomCode, playerName);
                this.roomCode = lobby.getRoomCode();
                this.playerIndex = lobby.getAssignedPlayerIndex();

                client.subscribe(this.roomCode, updateListener);

                Platform.runLater(() -> {
                    appState.setRoomCode(this.roomCode);
                    appState.setLocalPlayerIndex(playerIndex);
                    appState.setConnectionStatus("Connected");
                });

                return lobby;
            } catch (Exception e) {
                throw new RuntimeException("Failed to join game", e);
            }
        });
    }

    public void shutdown() {
        if (asyncService != null) asyncService.shutdown();
        if (server != null) server.stop();
    }

    private void createListener(
            Consumer<GameStateSnapshot> onStateChange,
            Consumer<ChatMessage> onChatMessage,
            Consumer<GameLobby> onGameStarted,
            Consumer<MoveResult> onGameOver) throws RemoteException {

        updateListener = new RemoteUpdateListener();
        updateListener.setOnStateUpdated(s -> { if (onStateChange != null) onStateChange.accept(s); });
        updateListener.setOnChatMessage(m -> { if (onChatMessage != null) onChatMessage.accept(m); });
        updateListener.setOnGameStarted(l -> { if (onGameStarted != null) onGameStarted.accept(l); });
        updateListener.setOnGameOver(r -> { if (onGameOver != null) onGameOver.accept(r); });
    }
}
