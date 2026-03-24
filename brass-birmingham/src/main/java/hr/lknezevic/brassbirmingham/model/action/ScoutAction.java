package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;

public record ScoutAction(
        Card discardedCard,
        Card additionalDiscard1,
        Card additionalDiscard2
) implements GameAction {}
