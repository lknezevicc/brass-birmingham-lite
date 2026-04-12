package hr.lknezevic.brassbirmingham.network.rmi;

import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.network.dto.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GameService extends Remote {

    GameLobby createGame(String playerName) throws RemoteException;

    GameLobby joinGame(String roomCode, String playerName) throws RemoteException;

    GameStateSnapshot getState(String roomCode) throws RemoteException;

    MoveResult submitMove(String roomCode, int playerIndex, GameAction action) throws RemoteException;

    void sendChat(String roomCode, int playerIndex, String message) throws RemoteException;

    List<LeaderboardEntry> getLeaderboard() throws RemoteException;

    void subscribeToUpdates(String roomCode, GameUpdateListener listener) throws RemoteException;

    void unsubscribeFromUpdates(String roomCode, GameUpdateListener listener) throws RemoteException;

    void saveGame(String roomCode) throws RemoteException;

    GameStateSnapshot loadGame(String roomCode) throws RemoteException;
}
