package hr.lknezevic.brassbirmingham.model.game;

import java.io.Serializable;

public final class Market implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int COAL_PRICE = 5;
    public static final int IRON_PRICE = 4;
    public static final int INITIAL_COAL = 4;
    public static final int INITIAL_IRON = 4;

    private int coalSupply;
    private int ironSupply;

    public Market() {
        this.coalSupply = INITIAL_COAL;
        this.ironSupply = INITIAL_IRON;
    }

    public int getCoalSupply() { return coalSupply; }
    public int getIronSupply() { return ironSupply; }

    public void buyCoal() {
        if (coalSupply > 0) coalSupply--;
    }

    public void buyIron() {
        if (ironSupply > 0) ironSupply--;
    }
}
