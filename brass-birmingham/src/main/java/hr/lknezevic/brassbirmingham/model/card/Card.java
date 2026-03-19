package hr.lknezevic.brassbirmingham.model.card;

import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.io.Serializable;

public record Card(CardType type, CityId city, IndustryType industry) implements Serializable {

    public static Card location(CityId city) {
        return new Card(CardType.LOCATION, city, null);
    }

    public static Card industry(IndustryType industry) {
        return new Card(CardType.INDUSTRY, null, industry);
    }
}
