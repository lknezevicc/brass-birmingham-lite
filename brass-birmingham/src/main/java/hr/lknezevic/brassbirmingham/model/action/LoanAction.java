package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;

public record LoanAction(
        Card discardedCard
) implements GameAction {}
