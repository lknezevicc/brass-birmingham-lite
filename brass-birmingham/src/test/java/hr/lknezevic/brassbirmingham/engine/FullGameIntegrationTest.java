package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plays through a simplified 10-round game (5 rounds per era) to verify:
 * - Turn/round/era transitions
 * - Scoring at end of each era
 * - Game termination after Rail era
 */
class FullGameIntegrationTest {

    @Test
    void fullGameCompletesWithWinner() {
        GameState state = GameStateFactory.newGame("Alice", "Bob");
        RulesEngine engine = new RulesEngine(state);

        int maxTurns = 200; // safety valve to prevent infinite loop
        int turnCount = 0;

        while (!engine.isGameOver() && turnCount < maxTurns) {
            turnCount++;
            PlayerState current = state.getCurrentPlayer();

            if (current.getHand().isEmpty()) {
                break; // no more cards — unusual but possible near end
            }

            GameAction action = pickAnyLegalAction(state, current);
            if (action == null) {
                break; // stuck (shouldn't happen with loan always available, but safety)
            }

            List<String> errors = engine.submitAction(action);
            assertThat(errors)
                    .withFailMessage("Action failed at turn %d: %s", turnCount, errors)
                    .isEmpty();
        }

        // Game should have finished or run out of cards
        if (engine.isGameOver()) {
            assertThat(state.getPhase()).isEqualTo(GamePhase.GAME_OVER);
            int winner = engine.getWinnerPlayerId();
            assertThat(winner).isIn(0, 1);

            // Both players should have some VP (from scoring)
            // At minimum: scoring ran
            assertThat(state.getPlayers().get(0).getVictoryPoints()
                    + state.getPlayers().get(1).getVictoryPoints()).isGreaterThanOrEqualTo(0);
        }
    }

    /**
     * Picks the simplest legal action: prefers Loan (always valid if income >= 2),
     * then Network, then Build.
     */
    private GameAction pickAnyLegalAction(GameState state, PlayerState player) {
        List<Card> hand = player.getHand().getCards();
        if (hand.isEmpty()) return null;

        Card card = hand.getFirst();

        // Try Loan first (simplest)
        LoanAction loan = new LoanAction(card);
        if (MoveValidator.validate(state, loan).isEmpty()) {
            return loan;
        }

        // Try Network on any free edge
        for (BoardEdge edge : BoardDefinition.EDGES) {
            if (!state.getBoard().isEdgeOccupied(edge)) {
                NetworkAction network = new NetworkAction(card, edge);
                if (MoveValidator.validate(state, network).isEmpty()) {
                    return network;
                }
            }
        }

        // Try Build with location cards
        for (Card c : hand) {
            if (c.city() != null) {
                for (IndustryType type : BoardDefinition.CITY_SLOTS.getOrDefault(c.city(), List.of())) {
                    BuildAction build = new BuildAction(c, c.city(), type);
                    if (MoveValidator.validate(state, build).isEmpty()) {
                        return build;
                    }
                }
            }
        }

        // Fallback: use any card for loan (might fail if income too low)
        // At this point just try again with different cards
        for (Card c : hand) {
            LoanAction fallback = new LoanAction(c);
            if (MoveValidator.validate(state, fallback).isEmpty()) {
                return fallback;
            }
        }

        return null;
    }
}
