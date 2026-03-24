package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;

public record NetworkAction(
        Card discardedCard,
        BoardEdge edge
) implements GameAction {}
