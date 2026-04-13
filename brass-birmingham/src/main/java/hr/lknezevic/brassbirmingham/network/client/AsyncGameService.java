package hr.lknezevic.brassbirmingham.network.client;

import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.network.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// RMI blocks — keep calls off the JavaFX thread
public final class AsyncGameService {

    private final RemoteGameClient client;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "rmi-async");
        t.setDaemon(true);
        return t;
    });

    public AsyncGameService(RemoteGameClient client) {
        this.client = client;
    }

    public CompletableFuture<GameLobby> createGame(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.createGame(playerName); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<GameLobby> joinGame(String roomCode, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.joinGame(roomCode, playerName); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<GameStateSnapshot> getState(String roomCode) {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.getState(roomCode); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<MoveResult> submitMove(String roomCode, int playerIndex, GameAction action) {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.submitMove(roomCode, playerIndex, action); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<Void> sendChat(String roomCode, int playerIndex, String message) {
        return CompletableFuture.runAsync(() -> {
            try { client.sendChat(roomCode, playerIndex, message); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<List<LeaderboardEntry>> getLeaderboard() {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.getLeaderboard(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<Void> saveGame(String roomCode) {
        return CompletableFuture.runAsync(() -> {
            try { client.saveGame(roomCode); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public CompletableFuture<GameStateSnapshot> loadGame(String roomCode) {
        return CompletableFuture.supplyAsync(() -> {
            try { return client.loadGame(roomCode); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
