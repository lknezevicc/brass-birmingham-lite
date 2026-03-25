package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.engine.validation.*;
import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

public final class MoveValidator {

    private MoveValidator() {}

    public static List<String> validate(GameState state, GameAction action) {
        PlayerState player = state.getCurrentPlayer();
        List<String> errors = new ArrayList<>();

        if (action.discardedCard() == null) {
            errors.add("Must discard a card to perform an action");
            return errors;
        }
        if (!player.getHand().contains(action.discardedCard())) {
            errors.add("Discarded card not in player's hand");
            return errors;
        }

        switch (action) {
            case BuildAction build -> BuildMoveValidator.validate(state, player, build, errors);
            case NetworkAction network -> NetworkMoveValidator.validate(state, player, network, errors);
            case SellAction sell -> SellMoveValidator.validate(state, player, sell, errors);
            case LoanAction loan -> LoanMoveValidator.validate(state, player, loan, errors);
            case ScoutAction scout -> ScoutMoveValidator.validate(state, player, scout, errors);
        }

        return errors;
    }
}
