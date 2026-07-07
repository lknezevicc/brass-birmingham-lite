package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.Era;
import hr.lknezevic.brassbirmingham.model.game.GamePhase;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.Hand;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

public final class TurnManager {

    private TurnManager() {}

    public static void advanceTurn(GameState state) {
        if (state.getActionsRemainingThisTurn() > 0) {
            if (!canPlayerAct(state)) {
                GameFlowLogger.event("Player {} has no cards and deck empty — skipping remaining actions", state.getCurrentPlayerIndex());
                state.setActionsRemainingThisTurn(0);
            } else {
                GameFlowLogger.event("Actions remaining: {}, no turn advance", state.getActionsRemainingThisTurn());
                return;
            }
        }

        GameFlowLogger.entering("player={}, round={}, era={}", state.getCurrentPlayerIndex(), state.getCurrentRound(), state.getCurrentEra());
        refillHand(state, state.getCurrentPlayer());

        int nextPlayer = 1 - state.getCurrentPlayerIndex();

        if (isEndOfRound(state, nextPlayer)) {
            endRound(state);
            autoSkipIfNeeded(state);
        } else {
            state.setCurrentPlayerIndex(nextPlayer);
            state.setActionsRemainingThisTurn(2);
            autoSkipIfNeeded(state);
        }
    }

    private static void autoSkipIfNeeded(GameState state) {
        if (state.getPhase() == GamePhase.GAME_OVER) return;
        if (!canPlayerAct(state)) {
            GameFlowLogger.event("Player {} cannot act (no cards) — auto-advancing", state.getCurrentPlayerIndex());
            state.setActionsRemainingThisTurn(0);
            advanceTurn(state);
        }
    }

    private static boolean canPlayerAct(GameState state) {
        return !state.getCurrentPlayer().getHand().isEmpty() || !state.getDeck().isEmpty();
    }

    private static boolean isEndOfRound(GameState state, int nextPlayerIndex) {
        return state.getCurrentPlayerIndex() == 1;
    }

    private static void endRound(GameState state) {
        for (PlayerState p : state.getPlayers()) {
            p.receiveIncome();
            p.resetSpentThisRound();
        }

        int round = state.getCurrentRound();
        int roundsPerEra = state.getRoundsPerEra();

        if (round >= roundsPerEra) {
            endEra(state);
        } else {
            state.setCurrentRound(round + 1);
            state.setCurrentPlayerIndex(determineFirstPlayer(state));
            state.setActionsRemainingThisTurn(2);
        }
    }

    private static void endEra(GameState state) {
        GameFlowLogger.event("Era ending: {}", state.getCurrentEra());
        ScoringService.scoreEra(state);

        if (state.getCurrentEra() == Era.RAIL) {
            GameFlowLogger.event("GAME OVER — final scores: P0={}, P1={}", state.getPlayers().get(0).getVictoryPoints(), state.getPlayers().get(1).getVictoryPoints());
            state.setPhase(GamePhase.GAME_OVER);
            return;
        }

        GameFlowLogger.event("Transitioning CANAL -> RAIL");
        transitionToRailEra(state);
    }

    private static void transitionToRailEra(GameState state) {
        state.getBoard().removeLevel1Industries();
        state.getBoard().removeAllLinks();
        state.getBoard().resetMerchantBeer();
        state.getDeck().reshuffleBetweenEras();

        for (PlayerState p : state.getPlayers()) {
            while (p.getHand().size() < Hand.MAX_SIZE) {
                Card card = state.getDeck().draw();
                if (card == null) break;
                p.getHand().add(card);
            }
        }

        state.setCurrentEra(Era.RAIL);
        state.setCurrentRound(1);
        state.setCurrentPlayerIndex(determineFirstPlayer(state));
        state.setActionsRemainingThisTurn(2);
    }

    private static void refillHand(GameState state, PlayerState player) {
        while (player.getHand().size() < Hand.MAX_SIZE) {
            Card card = state.getDeck().draw();
            if (card == null) break;
            player.getHand().add(card);
        }
    }

    private static int determineFirstPlayer(GameState state) {
        int spent0 = state.getPlayers().get(0).getSpentThisRound();
        int spent1 = state.getPlayers().get(1).getSpentThisRound();
        if (spent0 < spent1) return 0;
        if (spent1 < spent0) return 1;
        return 0;
    }
}
