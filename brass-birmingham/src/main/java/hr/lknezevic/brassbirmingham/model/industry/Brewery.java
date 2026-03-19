package hr.lknezevic.brassbirmingham.model.industry;

public class Brewery extends Industry {

    public Brewery(IndustryLevel level) {
        super(level);
    }

    @Override
    public IndustryType getType() {
        return IndustryType.BREWERY;
    }

    @Override
    public int getBuildCost() {
        return level == IndustryLevel.L1 ? 5 : 8;
    }

    @Override
    public int getIronRequired() {
        return 1;
    }

    @Override
    public int getCoalRequired() {
        return 0;
    }

    @Override
    public int getVictoryPoints() {
        return level == IndustryLevel.L1 ? 3 : 7;
    }

    @Override
    public int getIncomeBonus() {
        return level == IndustryLevel.L1 ? 2 : 3;
    }

    @Override
    public int getResourceCapacity() {
        return level == IndustryLevel.L1 ? 1 : 2;
    }

    @Override
    public FlipTrigger getFlipTrigger() {
        return FlipTrigger.RESOURCE_DEPLETION;
    }

    @Override
    public int getBeerRequired() {
        return 0;
    }
}
