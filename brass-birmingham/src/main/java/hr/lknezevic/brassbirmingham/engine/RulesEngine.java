package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.game.GamePhase;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class RulesEngine {

    @Getter
    private final GameState state;

    public RulesEngine(GameState state) {
        this.state = state;
    }

    public List<String> submitAction(GameAction action) {
        GameFlowLogger.entering("player={}, action={}", state.getCurrentPlayerIndex(), action.getClass().getSimpleName());
        if (state.getPhase() == GamePhase.GAME_OVER) {
            return List.of("Game is already finished");
        }

        List<String> errors = MoveValidator.validate(state, action);
        if (!errors.isEmpty()) {
            GameFlowLogger.exiting("rejected, errors={}", errors);
            log.debug("Move rejected: player={}, action={}, errors={}", state.getCurrentPlayerIndex(), action.getClass().getSimpleName(), errors);
            return errors;
        }

        ActionExecutor.execute(state, action);
        TurnManager.advanceTurn(state);
        GameFlowLogger.exiting("success, next player={}, era={}, round={}", state.getCurrentPlayerIndex(), state.getCurrentEra(), state.getCurrentRound());
        log.info("Move executed: player={}, action={}", state.getCurrentPlayerIndex(), action.getClass().getSimpleName());

        return List.of();
    }

    public boolean isGameOver() {
        return state.getPhase() == GamePhase.GAME_OVER;
    }

    public int getWinnerPlayerId() {
        if (!isGameOver()) return -1;
        var p0 = state.getPlayers().get(0);
        var p1 = state.getPlayers().get(1);
        if (p0.getVictoryPoints() > p1.getVictoryPoints()) return 0;
        if (p1.getVictoryPoints() > p0.getVictoryPoints()) return 1;
        return p0.getMoney() >= p1.getMoney() ? 0 : 1;
    }
}
