package hr.lknezevic.brassbirmingham.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppState {
    private final StringProperty playerName = new SimpleStringProperty("Player1");
    private final StringProperty roomCode = new SimpleStringProperty("---");
    private final StringProperty connectionStatus = new SimpleStringProperty("Offline");

    public String getPlayerName() {
        return playerName.get();
    }

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return;
        }

        this.playerName.set(playerName.trim());
    }

    public String getRoomCode() {
        return roomCode.get();
    }

    public StringProperty roomCodeProperty() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            this.roomCode.set("---");
            return;
        }

        this.roomCode.set(roomCode.trim());
    }

    public String getConnectionStatus() {
        return connectionStatus.get();
    }

    public StringProperty connectionStatusProperty() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        if (connectionStatus == null || connectionStatus.isBlank()) {
            this.connectionStatus.set("Offline");
            return;
        }

        this.connectionStatus.set(connectionStatus.trim());
    }
}
