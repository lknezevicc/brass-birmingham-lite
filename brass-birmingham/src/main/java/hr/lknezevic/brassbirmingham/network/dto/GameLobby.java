package hr.lknezevic.brassbirmingham.network.dto;

import java.io.Serializable;
import java.util.List;

public final class GameLobby implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { WAITING, IN_PROGRESS }

    private final String roomCode;
    private final int assignedPlayerIndex;
    private final List<String> playerNames;
    private final Status status;

    public GameLobby(String roomCode, int assignedPlayerIndex, List<String> playerNames, Status status) {
        this.roomCode = roomCode;
        this.assignedPlayerIndex = assignedPlayerIndex;
        this.playerNames = List.copyOf(playerNames);
        this.status = status;
    }

    public String getRoomCode() { return roomCode; }
    public int getAssignedPlayerIndex() { return assignedPlayerIndex; }
    public List<String> getPlayerNames() { return playerNames; }
    public Status getStatus() { return status; }
}
