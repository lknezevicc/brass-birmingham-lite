package hr.lknezevic.brassbirmingham.network.rmi;

import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.network.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GameServiceImpl extends UnicastRemoteObject implements GameService {

    private static final long serialVersionUID = 1L;

    private final transient GameRoomRegistry registry;
    private final List<LeaderboardEntry> leaderboard = new ArrayList<>();

    public GameServiceImpl(GameRoomRegistry registry) throws RemoteException {
        super();
        this.registry = registry;
    }

    @Override
    public GameLobby createGame(String playerName) throws RemoteException {
        GameRoom room = registry.createRoom(playerName);
        return room.toLobby(0);
    }

    @Override
    public GameLobby joinGame(String roomCode, String playerName) throws RemoteException {
        GameRoom room = registry.getRoom(roomCode);
        if (room == null) throw new RemoteException("Room not found: " + roomCode);
        if (room.isFull()) throw new RemoteException("Room is full");

        room.addPlayer(playerName);
        int playerIndex = 1;

        if (room.isFull()) {
            room.startGame();
            GameLobby lobby = room.toLobby(playerIndex);
            room.notifyGameStarted(lobby);
        }

        return room.toLobby(playerIndex);
    }

    @Override
    public GameStateSnapshot getState(String roomCode) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        if (room.getEngine() == null) throw new RemoteException("Game not started");
        return new GameStateSnapshot(room.getEngine().getState());
    }

    @Override
    public MoveResult submitMove(String roomCode, int playerIndex, GameAction action) throws RemoteException {
        log.info("Move received: room={}, player={}, action={}", roomCode, playerIndex, action.getClass().getSimpleName());
        GameRoom room = getExistingRoom(roomCode);
        if (room.getEngine() == null) throw new RemoteException("Game not started");

        ReentrantLock lock = room.getMoveLock();
        lock.lock();
        try {
            var engine = room.getEngine();
            var state = engine.getState();

            if (state.getCurrentPlayerIndex() != playerIndex) {
                return MoveResult.fail(List.of("Not your turn"));
            }

            List<String> errors = engine.submitAction(action);
            if (!errors.isEmpty()) {
                return MoveResult.fail(errors);
            }

            GameStateSnapshot snapshot = new GameStateSnapshot(state);
            boolean gameOver = engine.isGameOver();
            int winner = gameOver ? engine.getWinnerPlayerId() : -1;

            room.getReplayWriter().appendMove(state, playerIndex, action);

            MoveResult result = MoveResult.ok(snapshot, gameOver, winner);

            room.notifyStateUpdated(snapshot);

            if (gameOver) {
                int[] scores = state.getPlayers().stream()
                        .mapToInt(p -> p.getVictoryPoints()).toArray();
                room.getReplayWriter().finishGame(winner, scores);
                room.getReplayWriter().writeToFile(
                        new File("replays/game-" + roomCode + ".xml"));
                recordWinner(room, winner);
                room.notifyGameOver(result);
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void sendChat(String roomCode, int playerIndex, String message) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        String sender = room.getPlayerNames().get(playerIndex);
        ChatMessage msg = new ChatMessage(sender, message);
        room.addChatMessage(msg);
        room.notifyChatMessage(msg);
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard() throws RemoteException {
        synchronized (leaderboard) {
            return List.copyOf(leaderboard);
        }
    }

    @Override
    public void subscribeToUpdates(String roomCode, GameUpdateListener listener) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        room.addListener(listener);
    }

    @Override
    public void unsubscribeFromUpdates(String roomCode, GameUpdateListener listener) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        room.removeListener(listener);
    }

    @Override
    public void saveGame(String roomCode) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        if (room.getEngine() == null) throw new RemoteException("Game not started");
        ReentrantLock lock = room.getMoveLock();
        lock.lock();
        try {
            new hr.lknezevic.brassbirmingham.persistence.save.SaveGameService()
                    .save(room.getEngine().getState(), new File("saves/game-" + roomCode + ".ser"));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GameStateSnapshot loadGame(String roomCode) throws RemoteException {
        GameRoom room = getExistingRoom(roomCode);
        File saveFile = new File("saves/game-" + roomCode + ".ser");
        if (!saveFile.exists()) throw new RemoteException("No save file found for room: " + roomCode);
        ReentrantLock lock = room.getMoveLock();
        lock.lock();
        try {
            var state = new hr.lknezevic.brassbirmingham.persistence.save.LoadGameService().load(saveFile);
            room.replaceEngine(state);
            GameStateSnapshot snapshot = new GameStateSnapshot(state);
            room.notifyStateUpdated(snapshot);
            return snapshot;
        } finally {
            lock.unlock();
        }
    }

    private GameRoom getExistingRoom(String roomCode) throws RemoteException {
        GameRoom room = registry.getRoom(roomCode);
        if (room == null) throw new RemoteException("Room not found: " + roomCode);
        return room;
    }

    private void recordWinner(GameRoom room, int winnerIndex) {
        String name = room.getPlayerNames().get(winnerIndex);
        int vp = room.getEngine().getState().getPlayers().get(winnerIndex).getVictoryPoints();
        synchronized (leaderboard) {
            leaderboard.add(new LeaderboardEntry(name, vp));
            leaderboard.sort((a, b) -> Integer.compare(b.getVictoryPoints(), a.getVictoryPoints()));
        }
    }
}
