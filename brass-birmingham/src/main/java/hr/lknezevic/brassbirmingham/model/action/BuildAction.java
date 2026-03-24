package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

public record BuildAction(
        Card discardedCard,
        CityId targetCity,
        IndustryType industryType
) implements GameAction {}
