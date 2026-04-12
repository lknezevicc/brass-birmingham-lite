package hr.lknezevic.brassbirmingham.network.rmi;

import hr.lknezevic.brassbirmingham.network.dto.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameUpdateListener extends Remote {

    void onStateUpdated(GameStateSnapshot snapshot) throws RemoteException;

    void onChatMessage(ChatMessage message) throws RemoteException;

    void onGameStarted(GameLobby lobby) throws RemoteException;

    void onGameOver(MoveResult result) throws RemoteException;
}
