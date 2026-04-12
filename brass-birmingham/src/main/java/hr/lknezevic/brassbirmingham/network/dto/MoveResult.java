package hr.lknezevic.brassbirmingham.network.dto;

import java.io.Serializable;
import java.util.List;

public final class MoveResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final List<String> errors;
    private final GameStateSnapshot snapshot;
    private final boolean gameOver;
    private final int winnerPlayerId;

    public MoveResult(boolean success, List<String> errors, GameStateSnapshot snapshot, boolean gameOver, int winnerPlayerId) {
        this.success = success;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.snapshot = snapshot;
        this.gameOver = gameOver;
        this.winnerPlayerId = winnerPlayerId;
    }

    public static MoveResult ok(GameStateSnapshot snapshot, boolean gameOver, int winnerPlayerId) {
        return new MoveResult(true, List.of(), snapshot, gameOver, winnerPlayerId);
    }

    public static MoveResult fail(List<String> errors) {
        return new MoveResult(false, errors, null, false, -1);
    }

    public boolean isSuccess() { return success; }
    public List<String> getErrors() { return errors; }
    public GameStateSnapshot getSnapshot() { return snapshot; }
    public boolean isGameOver() { return gameOver; }
    public int getWinnerPlayerId() { return winnerPlayerId; }
}
