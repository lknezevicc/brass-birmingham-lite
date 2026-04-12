package hr.lknezevic.brassbirmingham.network.rmi;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public final class GameRoom {

    private final String roomCode;
    private final List<String> playerNames = new ArrayList<>(2);
    private final CopyOnWriteArrayList<GameUpdateListener> listeners = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> chatHistory = new ArrayList<>();
    private final ReentrantLock moveLock = new ReentrantLock();
    private final ReplayWriter replayWriter = new ReplayWriter();

    private RulesEngine engine;
    private GameLobby.Status status;

    public GameRoom(String roomCode, String hostName) {
        this.roomCode = roomCode;
        this.playerNames.add(hostName);
        this.status = GameLobby.Status.WAITING;
    }

    public String getRoomCode() { return roomCode; }
    public GameLobby.Status getStatus() { return status; }
    public List<String> getPlayerNames() { return List.copyOf(playerNames); }
    public RulesEngine getEngine() { return engine; }
    public ReentrantLock getMoveLock() { return moveLock; }
    public List<ChatMessage> getChatHistory() { return List.copyOf(chatHistory); }
    public ReplayWriter getReplayWriter() { return replayWriter; }

    public boolean isFull() {
        return playerNames.size() >= 2;
    }

    public void addPlayer(String name) {
        if (isFull()) throw new IllegalStateException("Room is full");
        playerNames.add(name);
    }

    public void startGame() {
        GameFlowLogger.network("Room {} starting game: {}", roomCode, playerNames);
        GameState state = GameStateFactory.newGame(playerNames.get(0), playerNames.get(1));
        this.engine = new RulesEngine(state);
        this.status = GameLobby.Status.IN_PROGRESS;
        replayWriter.startGame(List.copyOf(playerNames), state.getDeck().getSeed());
    }

    public void replaceEngine(GameState state) {
        this.engine = new RulesEngine(state);
    }

    public void addListener(GameUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameUpdateListener listener) {
        listeners.remove(listener);
    }

    public void addChatMessage(ChatMessage msg) {
        chatHistory.add(msg);
    }

    public void notifyStateUpdated(GameStateSnapshot snapshot) {
        for (GameUpdateListener l : listeners) {
            try {
                l.onStateUpdated(snapshot);
            } catch (Exception e) {
                dropDeadListener(l, e);
            }
        }
    }

    public void notifyChatMessage(ChatMessage msg) {
        for (GameUpdateListener l : listeners) {
            try {
                l.onChatMessage(msg);
            } catch (Exception e) {
                dropDeadListener(l, e);
            }
        }
    }

    public void notifyGameStarted(GameLobby lobby) {
        for (GameUpdateListener l : listeners) {
            try {
                l.onGameStarted(lobby);
            } catch (Exception e) {
                dropDeadListener(l, e);
            }
        }
    }

    public void notifyGameOver(MoveResult result) {
        for (GameUpdateListener l : listeners) {
            try {
                l.onGameOver(result);
            } catch (Exception e) {
                dropDeadListener(l, e);
            }
        }
    }
    
    private void dropDeadListener(GameUpdateListener listener, Exception cause) {
        log.debug("Removing unreachable listener from room {}: {}", roomCode, cause.toString());
        listeners.remove(listener);
    }

    public GameLobby toLobby(int playerIndex) {
        return new GameLobby(roomCode, playerIndex, getPlayerNames(), status);
    }
}
