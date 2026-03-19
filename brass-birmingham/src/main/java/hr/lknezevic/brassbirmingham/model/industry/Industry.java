package hr.lknezevic.brassbirmingham.model.industry;

import java.io.Serializable;

public abstract class Industry implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final IndustryLevel level;

    protected Industry(IndustryLevel level) {
        this.level = level;
    }

    public IndustryLevel getLevel() {
        return level;
    }

    public abstract IndustryType getType();

    public abstract int getBuildCost();

    public abstract int getIronRequired();

    public abstract int getCoalRequired();

    public abstract int getVictoryPoints();

    public abstract int getIncomeBonus();

    public abstract int getResourceCapacity();

    public abstract FlipTrigger getFlipTrigger();

    public abstract int getBeerRequired();

    public static Industry create(IndustryType type, IndustryLevel level) {
        return switch (type) {
            case COAL_MINE -> new CoalMine(level);
            case IRON_WORKS -> new IronWorks(level);
            case BREWERY -> new Brewery(level);
            case COTTON_MILL -> new CottonMill(level);
        };
    }
}
