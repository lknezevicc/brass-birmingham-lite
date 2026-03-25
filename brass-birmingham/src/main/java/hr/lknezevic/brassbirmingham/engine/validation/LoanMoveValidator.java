package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class LoanMoveValidator {

    private LoanMoveValidator() {}

    public static void validate(GameState state, PlayerState player, LoanAction action, List<String> errors) {
        if (player.getIncomeLevel() - 2 < 0) {
            errors.add("Cannot take loan: income would drop below 0");
        }
    }
}
