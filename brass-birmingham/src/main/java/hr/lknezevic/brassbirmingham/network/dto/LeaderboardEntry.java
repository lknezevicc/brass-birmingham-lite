package hr.lknezevic.brassbirmingham.network.dto;

import java.io.Serializable;
import java.time.Instant;

public final class LeaderboardEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final int victoryPoints;
    private final long playedAtEpochMilli;

    public LeaderboardEntry(String playerName, int victoryPoints) {
        this.playerName = playerName;
        this.victoryPoints = victoryPoints;
        this.playedAtEpochMilli = Instant.now().toEpochMilli();
    }

    public String getPlayerName() { return playerName; }
    public int getVictoryPoints() { return victoryPoints; }
    public long getPlayedAtEpochMilli() { return playedAtEpochMilli; }
}
