package hr.lknezevic.brassbirmingham.model.game;

import hr.lknezevic.brassbirmingham.model.card.Deck;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public final class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<PlayerState> players;
    private final Board board;
    private final Deck deck;
    private final CityNetwork cityNetwork;

    private Era currentEra;
    private int currentRound;
    private int currentPlayerIndex;
    private int actionsRemainingThisTurn;
    private GamePhase phase;

    public GameState(List<PlayerState> players, Board board, Deck deck, CityNetwork cityNetwork) {
        this.players = players;
        this.board = board;
        this.deck = deck;
        this.cityNetwork = cityNetwork;
        this.currentEra = Era.CANAL;
        this.currentRound = 1;
        this.currentPlayerIndex = 0;
        this.actionsRemainingThisTurn = 1;
        this.phase = GamePhase.IN_PROGRESS;
    }

    public PlayerState getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public PlayerState getOpponent() {
        return players.get(1 - currentPlayerIndex);
    }

    public void setCurrentEra(Era era) { this.currentEra = era; }
    public void setCurrentRound(int round) { this.currentRound = round; }
    public void setCurrentPlayerIndex(int index) { this.currentPlayerIndex = index; }
    public void setActionsRemainingThisTurn(int actions) { this.actionsRemainingThisTurn = actions; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    public void decrementActions() {
        this.actionsRemainingThisTurn--;
    }

    public boolean isFirstRoundOfCanalEra() {
        return currentEra == Era.CANAL && currentRound == 1;
    }

    public int getRoundsPerEra() {
        return 5;
    }
}
