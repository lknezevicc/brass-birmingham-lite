package hr.lknezevic.brassbirmingham.persistence.replay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ReplayDocument {

    private String date;
    private List<String> playerNames = new ArrayList<>();
    private String mapId = "lite-5-cities";
    private long deckSeed;
    private final List<ReplayMove> moves = new ArrayList<>();
    private int winnerIndex = -1;
    private String finalScores;

    public ReplayDocument() {
        this.date = Instant.now().toString();
    }

    public ReplayDocument(String date, List<String> playerNames, String mapId) {
        this.date = date;
        this.playerNames = new ArrayList<>(playerNames);
        this.mapId = mapId;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<String> getPlayerNames() { return playerNames; }
    public void setPlayerNames(List<String> names) { this.playerNames = new ArrayList<>(names); }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public long getDeckSeed() { return deckSeed; }
    public void setDeckSeed(long deckSeed) { this.deckSeed = deckSeed; }
    public List<ReplayMove> getMoves() { return moves; }
    public int getWinnerIndex() { return winnerIndex; }
    public void setWinnerIndex(int winnerIndex) { this.winnerIndex = winnerIndex; }
    public String getFinalScores() { return finalScores; }
    public void setFinalScores(String finalScores) { this.finalScores = finalScores; }

    public void addMove(ReplayMove move) {
        moves.add(move);
    }
}
