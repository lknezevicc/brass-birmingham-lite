package hr.lknezevic.brassbirmingham.model.industry;

public class CottonMill extends Industry {

    public CottonMill(IndustryLevel level) {
        super(level);
    }

    @Override
    public IndustryType getType() {
        return IndustryType.COTTON_MILL;
    }

    @Override
    public int getBuildCost() {
        return level == IndustryLevel.L1 ? 6 : 10;
    }

    @Override
    public int getIronRequired() {
        return level == IndustryLevel.L1 ? 0 : 1;
    }

    @Override
    public int getCoalRequired() {
        return level == IndustryLevel.L1 ? 0 : 1;
    }

    @Override
    public int getVictoryPoints() {
        return level == IndustryLevel.L1 ? 4 : 8;
    }

    @Override
    public int getIncomeBonus() {
        return level == IndustryLevel.L1 ? 2 : 3;
    }

    @Override
    public int getResourceCapacity() {
        return 0;
    }

    @Override
    public FlipTrigger getFlipTrigger() {
        return FlipTrigger.SELL_ACTION;
    }

    @Override
    public int getBeerRequired() {
        return level == IndustryLevel.L1 ? 0 : 1;
    }
}
