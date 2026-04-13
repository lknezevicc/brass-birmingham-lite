package hr.lknezevic.brassbirmingham.network.client;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.network.jndi.JndiConfig;
import hr.lknezevic.brassbirmingham.network.rmi.GameService;
import hr.lknezevic.brassbirmingham.network.rmi.GameUpdateListener;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;

public final class RemoteGameClient {

    private GameService service;

    public void connect(String host, int port) throws NamingException {
        GameFlowLogger.network("Connecting to {}:{} via JNDI", host, port);
        this.service = JndiConfig.lookup(host, port);
        GameFlowLogger.network("Connected, service stub obtained");
    }

    public boolean isConnected() {
        return service != null;
    }

    public GameLobby createGame(String playerName) throws RemoteException {
        return service.createGame(playerName);
    }

    public GameLobby joinGame(String roomCode, String playerName) throws RemoteException {
        return service.joinGame(roomCode, playerName);
    }

    public GameStateSnapshot getState(String roomCode) throws RemoteException {
        return service.getState(roomCode);
    }

    public MoveResult submitMove(String roomCode, int playerIndex, GameAction action) throws RemoteException {
        GameFlowLogger.network("submitMove room={}, player={}, action={}", roomCode, playerIndex, action.getClass().getSimpleName());
        return service.submitMove(roomCode, playerIndex, action);
    }

    public void sendChat(String roomCode, int playerIndex, String message) throws RemoteException {
        service.sendChat(roomCode, playerIndex, message);
    }

    public List<LeaderboardEntry> getLeaderboard() throws RemoteException {
        return service.getLeaderboard();
    }

    public void subscribe(String roomCode, GameUpdateListener listener) throws RemoteException {
        service.subscribeToUpdates(roomCode, listener);
    }

    public void unsubscribe(String roomCode, GameUpdateListener listener) throws RemoteException {
        service.unsubscribeFromUpdates(roomCode, listener);
    }

    public void saveGame(String roomCode) throws RemoteException {
        service.saveGame(roomCode);
    }

    public GameStateSnapshot loadGame(String roomCode) throws RemoteException {
        return service.loadGame(roomCode);
    }
}
