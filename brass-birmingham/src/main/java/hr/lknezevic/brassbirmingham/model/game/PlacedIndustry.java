package hr.lknezevic.brassbirmingham.model.game;

import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import lombok.Getter;

import java.io.Serializable;

@Getter
public final class PlacedIndustry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int ownerId;
    private final CityId city;
    private final IndustryType type;
    private final IndustryLevel level;
    private boolean flipped;
    private int remainingResources;

    public PlacedIndustry(int ownerId, CityId city, IndustryType type, IndustryLevel level, int initialResources) {
        this.ownerId = ownerId;
        this.city = city;
        this.type = type;
        this.level = level;
        this.flipped = false;
        this.remainingResources = initialResources;
    }

    public void consumeResource() {
        if (remainingResources > 0) {
            remainingResources--;
        }
    }

    public void flip() {
        this.flipped = true;
    }

    public boolean isResourceDepleted() {
        return remainingResources <= 0 && !flipped;
    }
}
