package hr.lknezevic.brassbirmingham.app;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayWriter;
import hr.lknezevic.brassbirmingham.persistence.save.LoadGameService;
import hr.lknezevic.brassbirmingham.persistence.save.SaveGameService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public final class GameSession {

    private final AppState appState;
    private final GameSessionNetworkHelper networkHelper = new GameSessionNetworkHelper();

    private RulesEngine localEngine;
    private ReplayWriter localReplayWriter;
    private final AtomicReference<GameStateSnapshot> lastSnapshot = new AtomicReference<>();

    private Consumer<GameStateSnapshot> onStateChange;
    private Consumer<ChatMessage> onChatMessage;
    private Consumer<GameLobby> onGameStarted;
    private Consumer<MoveResult> onGameOver;

    public GameSession(AppState appState) {
        this.appState = appState;
    }

    public void setOnStateChange(Consumer<GameStateSnapshot> handler) { this.onStateChange = handler; }
    public void setOnChatMessage(Consumer<ChatMessage> handler) { this.onChatMessage = handler; }
    public void setOnGameStarted(Consumer<GameLobby> handler) { this.onGameStarted = handler; }
    public void setOnGameOver(Consumer<MoveResult> handler) { this.onGameOver = handler; }

    public void startLocalGame(String player1, String player2) {
        GameFlowLogger.entering("{} vs {}", player1, player2);
        appState.setSessionMode(SessionMode.LOCAL);
        GameState state = GameStateFactory.newGame(player1, player2);
        localEngine = new RulesEngine(state);
        localReplayWriter = new ReplayWriter();
        localReplayWriter.startGame(List.of(player1, player2), state.getDeck().getSeed());
        GameFlowLogger.event("Local game initialized, seed={}", state.getDeck().getSeed());
        log.info("Local game started: {} vs {}", player1, player2);
    }

    public CompletableFuture<GameLobby> hostOnlineGame(String playerName) {
        GameFlowLogger.network("Hosting game as '{}'", playerName);
        appState.setSessionMode(SessionMode.ONLINE);
        return networkHelper.hostOnlineGame(appState, playerName,
                this::handleStateUpdate, onChatMessage, onGameStarted, onGameOver);
    }

    public CompletableFuture<GameLobby> joinOnlineGame(String host, int port, String roomCode, String playerName) {
        GameFlowLogger.network("Joining {}:{} room={} as '{}'", host, port, roomCode, playerName);
        appState.setSessionMode(SessionMode.ONLINE);
        return networkHelper.joinOnlineGame(appState, host, port, roomCode, playerName,
                this::handleStateUpdate, onChatMessage, onGameStarted, onGameOver);
    }

    public CompletableFuture<MoveResult> submitAction(GameAction action) {
        GameFlowLogger.entering("mode={}, action={}", appState.getSessionMode(), action.getClass().getSimpleName());
        if (appState.getSessionMode() == SessionMode.LOCAL) {
            return CompletableFuture.completedFuture(executeLocal(action));
        }
        return networkHelper.getAsyncService()
                .submitMove(networkHelper.getRoomCode(), networkHelper.getPlayerIndex(), action);
    }

    public CompletableFuture<Void> sendChat(String message) {
        if (appState.getSessionMode() == SessionMode.LOCAL) {
            return CompletableFuture.completedFuture(null);
        }
        return networkHelper.getAsyncService()
                .sendChat(networkHelper.getRoomCode(), networkHelper.getPlayerIndex(), message);
    }

    public GameState getCurrentState() {
        if (appState.getSessionMode() == SessionMode.LOCAL) {
            return localEngine != null ? localEngine.getState() : null;
        }
        GameStateSnapshot snap = lastSnapshot.get();
        return snap != null ? snap.getState() : null;
    }

    public GameStateSnapshot getLastSnapshot() { return lastSnapshot.get(); }
    public RulesEngine getLocalEngine() { return localEngine; }
    public ReplayWriter getLocalReplayWriter() { return localReplayWriter; }
    public boolean isOnline() { return appState.getSessionMode() == SessionMode.ONLINE; }

    public boolean isMyTurn() {
        GameState state = getCurrentState();
        if (state == null) return false;
        if (appState.getSessionMode() == SessionMode.LOCAL) return true;
        return state.getCurrentPlayerIndex() == appState.getLocalPlayerIndex();
    }

    public CompletableFuture<GameStateSnapshot> fetchState() {
        if (!isOnline() || networkHelper.getAsyncService() == null) {
            return CompletableFuture.completedFuture(null);
        }
        return networkHelper.getAsyncService().getState(networkHelper.getRoomCode()).thenApply(s -> {
            lastSnapshot.set(s);
            return s;
        });
    }

    public CompletableFuture<List<LeaderboardEntry>> getLeaderboard() {
        if (networkHelper.getAsyncService() == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return networkHelper.getAsyncService().getLeaderboard();
    }

    public void saveLocalGame() {
        if (localEngine == null) return;
        String path = appState.getSaveFilePath();
        GameFlowLogger.event("Saving game to {}", path);
        new SaveGameService().save(localEngine.getState(), new File(path));
        log.info("Game saved to {}", path);
    }

    public void loadLocalGame(File file) {
        GameFlowLogger.entering("file={}", file.getName());
        GameState state = new LoadGameService().load(file);
        appState.setSessionMode(SessionMode.LOCAL);
        localEngine = new RulesEngine(state);
        localReplayWriter = new ReplayWriter();
        log.info("Game loaded from {}", file.getAbsolutePath());
        localReplayWriter.startGame(
                state.getPlayers().stream().map(p -> p.getName()).toList(),
                state.getDeck().getSeed());
        GameStateSnapshot snapshot = new GameStateSnapshot(state);
        lastSnapshot.set(snapshot);
        if (onStateChange != null) Platform.runLater(() -> onStateChange.accept(snapshot));
    }

    public void shutdown() {
        networkHelper.shutdown();
    }

    private void handleStateUpdate(GameStateSnapshot snapshot) {
        lastSnapshot.set(snapshot);
        if (onStateChange != null) onStateChange.accept(snapshot);
    }

    private MoveResult executeLocal(GameAction action) {
        GameState stateBefore = localEngine.getState();
        int playerIdx = stateBefore.getCurrentPlayerIndex();

        List<String> errors = localEngine.submitAction(action);
        if (!errors.isEmpty()) {
            return MoveResult.fail(errors);
        }

        if (localReplayWriter != null) {
            localReplayWriter.appendMove(stateBefore, playerIdx, action);
        }

        GameStateSnapshot snapshot = new GameStateSnapshot(localEngine.getState());
        lastSnapshot.set(snapshot);
        boolean gameOver = localEngine.isGameOver();
        int winner = gameOver ? localEngine.getWinnerPlayerId() : -1;

        if (gameOver && localReplayWriter != null) {
            int[] scores = localEngine.getState().getPlayers().stream()
                    .mapToInt(p -> p.getVictoryPoints()).toArray();
            localReplayWriter.finishGame(winner, scores);
            String path = appState.getReplayFilePath();
            localReplayWriter.writeToFile(new File(path));
        }

        MoveResult result = MoveResult.ok(snapshot, gameOver, winner);
        if (onStateChange != null) Platform.runLater(() -> onStateChange.accept(snapshot));
        if (gameOver && onGameOver != null) Platform.runLater(() -> onGameOver.accept(result));
        return result;
    }
}
