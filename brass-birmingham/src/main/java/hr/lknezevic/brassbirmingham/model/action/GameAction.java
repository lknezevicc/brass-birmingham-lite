package hr.lknezevic.brassbirmingham.model.action;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.io.Serializable;

public sealed interface GameAction extends Serializable
        permits BuildAction, NetworkAction, SellAction, LoanAction, ScoutAction {

    Card discardedCard();
}
