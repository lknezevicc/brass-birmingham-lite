package hr.lknezevic.brassbirmingham.model.player;

import hr.lknezevic.brassbirmingham.model.game.Era;
import lombok.Getter;

import java.io.Serializable;

@Getter
public final class PlayerState implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STARTING_MONEY = 17;
    public static final int STARTING_INCOME = 10;
    public static final int CANAL_LINKS_TOTAL = 5;
    public static final int RAIL_LINKS_TOTAL = 5;

    private final String name;
    private final int playerId;
    private final Hand hand;
    private final PlayerMat mat;

    private int money;
    private int incomeLevel;
    private int victoryPoints;
    private int spentThisRound;
    private int canalLinksRemaining;
    private int railLinksRemaining;

    public PlayerState(String name, int playerId) {
        this.name = name;
        this.playerId = playerId;
        this.hand = new Hand();
        this.mat = new PlayerMat();
        this.money = STARTING_MONEY;
        this.incomeLevel = STARTING_INCOME;
        this.victoryPoints = 0;
        this.spentThisRound = 0;
        this.canalLinksRemaining = CANAL_LINKS_TOTAL;
        this.railLinksRemaining = RAIL_LINKS_TOTAL;
    }

    public void spend(int amount) {
        this.money -= amount;
        this.spentThisRound += amount;
    }

    public void earn(int amount) {
        this.money += amount;
    }

    public void addVictoryPoints(int vp) {
        this.victoryPoints += vp;
    }

    public void increaseIncome(int spaces) {
        this.incomeLevel = Math.min(30, this.incomeLevel + spaces);
    }

    public void decreaseIncomeLevel(int levels) {
        this.incomeLevel = Math.max(0, this.incomeLevel - levels);
    }

    public void resetSpentThisRound() {
        this.spentThisRound = 0;
    }

    public void useCanalLink() {
        if (canalLinksRemaining > 0) canalLinksRemaining--;
    }

    public void useRailLink() {
        if (railLinksRemaining > 0) railLinksRemaining--;
    }

    public boolean hasLinksRemaining(Era era) {
        return era == Era.CANAL ? canalLinksRemaining > 0 : railLinksRemaining > 0;
    }

    public int getIncomeAmount() {
        if (incomeLevel <= 0) return 0;
        if (incomeLevel <= 2) return 1;
        if (incomeLevel <= 4) return 2;
        if (incomeLevel <= 6) return 3;
        if (incomeLevel <= 8) return 4;
        if (incomeLevel <= 10) return 5;
        if (incomeLevel <= 13) return 6;
        if (incomeLevel <= 16) return 7;
        if (incomeLevel <= 19) return 8;
        if (incomeLevel <= 23) return 9;
        if (incomeLevel <= 27) return 10;
        return 12;
    }

    public void receiveIncome() {
        this.money += getIncomeAmount();
    }
}
