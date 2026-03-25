package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.model.action.ScoutAction;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class ScoutMoveValidator {

    private ScoutMoveValidator() {}

    public static void validate(GameState state, PlayerState player, ScoutAction action, List<String> errors) {
        if (player.getHand().size() < 3) {
            errors.add("Need at least 3 cards in hand to scout (1 discard + 2 additional)");
            return;
        }
        if (action.additionalDiscard1() == null || action.additionalDiscard2() == null) {
            errors.add("Must specify 2 additional cards to discard for scout");
            return;
        }
        if (!player.getHand().contains(action.additionalDiscard1()) ||
                !player.getHand().contains(action.additionalDiscard2())) {
            errors.add("Additional discard cards not in hand");
        }
        if (action.additionalDiscard1().equals(action.additionalDiscard2())) {
            errors.add("Cannot discard the same card twice");
        }
        if (action.additionalDiscard1().equals(action.discardedCard()) ||
                action.additionalDiscard2().equals(action.discardedCard())) {
            errors.add("Additional discards cannot be the same as the primary discard");
        }
    }
}
