package hr.lknezevic.brassbirmingham.network.client;

import hr.lknezevic.brassbirmingham.network.dto.*;
import hr.lknezevic.brassbirmingham.network.rmi.GameUpdateListener;
import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

public class RemoteUpdateListener extends UnicastRemoteObject implements GameUpdateListener {

    private static final long serialVersionUID = 1L;

    private transient Consumer<GameStateSnapshot> onStateUpdated;
    private transient Consumer<ChatMessage> onChatMessage;
    private transient Consumer<GameLobby> onGameStarted;
    private transient Consumer<MoveResult> onGameOver;

    public RemoteUpdateListener() throws RemoteException {
        super();
    }

    public void setOnStateUpdated(Consumer<GameStateSnapshot> handler) { this.onStateUpdated = handler; }
    public void setOnChatMessage(Consumer<ChatMessage> handler) { this.onChatMessage = handler; }
    public void setOnGameStarted(Consumer<GameLobby> handler) { this.onGameStarted = handler; }
    public void setOnGameOver(Consumer<MoveResult> handler) { this.onGameOver = handler; }

    @Override
    public void onStateUpdated(GameStateSnapshot snapshot) throws RemoteException {
        if (onStateUpdated != null) Platform.runLater(() -> onStateUpdated.accept(snapshot));
    }

    @Override
    public void onChatMessage(ChatMessage message) throws RemoteException {
        if (onChatMessage != null) Platform.runLater(() -> onChatMessage.accept(message));
    }

    @Override
    public void onGameStarted(GameLobby lobby) throws RemoteException {
        if (onGameStarted != null) Platform.runLater(() -> onGameStarted.accept(lobby));
    }

    @Override
    public void onGameOver(MoveResult result) throws RemoteException {
        if (onGameOver != null) Platform.runLater(() -> onGameOver.accept(result));
    }
}
