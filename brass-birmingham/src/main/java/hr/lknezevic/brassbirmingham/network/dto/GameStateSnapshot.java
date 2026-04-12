package hr.lknezevic.brassbirmingham.network.dto;

import hr.lknezevic.brassbirmingham.model.game.GameState;

import java.io.Serializable;

public final class GameStateSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final GameState state;

    public GameStateSnapshot(GameState state) {
        this.state = state;
    }

    public GameState getState() { return state; }
}
