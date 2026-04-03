package hr.lknezevic.brassbirmingham.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppState {
    private final StringProperty playerName = new SimpleStringProperty("Player1");
    private final StringProperty roomCode = new SimpleStringProperty("---");
    private final StringProperty connectionStatus = new SimpleStringProperty("Offline");
    private final StringProperty networkHost = new SimpleStringProperty("127.0.0.1");
    private final StringProperty networkPort = new SimpleStringProperty("1099");
    private final StringProperty replayFilePath = new SimpleStringProperty("replays/latest-replay.xml");
    private final StringProperty saveFilePath = new SimpleStringProperty("saves/latest-game.ser");
    private final StringProperty reportFilePath = new SimpleStringProperty("reports/industry-report.html");

    private SessionMode sessionMode = SessionMode.LOCAL;
    private int localPlayerIndex = 0;

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

    public String getNetworkHost() {
        return networkHost.get();
    }

    public StringProperty networkHostProperty() {
        return networkHost;
    }

    public void setNetworkHost(String networkHost) {
        if (networkHost == null || networkHost.isBlank()) {
            this.networkHost.set("127.0.0.1");
            return;
        }

        this.networkHost.set(networkHost.trim());
    }

    public String getNetworkPort() {
        return networkPort.get();
    }

    public StringProperty networkPortProperty() {
        return networkPort;
    }

    public void setNetworkPort(String networkPort) {
        if (networkPort == null || networkPort.isBlank()) {
            this.networkPort.set("1099");
            return;
        }

        this.networkPort.set(networkPort.trim());
    }

    public String getReplayFilePath() {
        return replayFilePath.get();
    }

    public StringProperty replayFilePathProperty() {
        return replayFilePath;
    }

    public void setReplayFilePath(String replayFilePath) {
        if (replayFilePath == null || replayFilePath.isBlank()) {
            this.replayFilePath.set("replays/latest-replay.xml");
            return;
        }

        this.replayFilePath.set(replayFilePath.trim());
    }

    public String getSaveFilePath() {
        return saveFilePath.get();
    }

    public StringProperty saveFilePathProperty() {
        return saveFilePath;
    }

    public void setSaveFilePath(String path) {
        if (path == null || path.isBlank()) {
            this.saveFilePath.set("saves/latest-game.ser");
            return;
        }
        this.saveFilePath.set(path.trim());
    }

    public String getReportFilePath() {
        return reportFilePath.get();
    }

    public StringProperty reportFilePathProperty() {
        return reportFilePath;
    }

    public void setReportFilePath(String path) {
        if (path == null || path.isBlank()) {
            this.reportFilePath.set("reports/industry-report.html");
            return;
        }
        this.reportFilePath.set(path.trim());
    }

    public SessionMode getSessionMode() {
        return sessionMode;
    }

    public void setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode == null ? SessionMode.LOCAL : sessionMode;
    }

    public int getLocalPlayerIndex() {
        return localPlayerIndex;
    }

    public void setLocalPlayerIndex(int localPlayerIndex) {
        this.localPlayerIndex = Math.max(0, localPlayerIndex);
    }
}
