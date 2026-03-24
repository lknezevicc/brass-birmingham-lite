package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.util.List;

public record SellAction(
        Card discardedCard,
        CityId sellingCity,
        IndustryType sellingType,
        List<CityId> beerSources
) implements GameAction {}
