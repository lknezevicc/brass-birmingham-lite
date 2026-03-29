package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.action.ScoutAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanAndScoutTest {

    private GameState state;
    private RulesEngine engine;

    @BeforeEach
    void setup() {
        state = GameStateFactory.newGame("Alice", "Bob");
        engine = new RulesEngine(state);
    }

    @Test
    void loanGives20AndDecreases2IncomeLevel() {
        int moneyBefore = state.getCurrentPlayer().getMoney();
        int incomeBefore = state.getCurrentPlayer().getIncomeLevel();

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        engine.submitAction(new LoanAction(card));

        assertThat(state.getPlayers().get(0).getMoney()).isEqualTo(moneyBefore + 20);
        assertThat(state.getPlayers().get(0).getIncomeLevel()).isEqualTo(incomeBefore - 2);
    }

    @Test
    void loanFailsIfIncomeWouldGoBelowZero() {
        // Set income to 1, loan would make it -1
        state.getPlayers().get(0).decreaseIncomeLevel(9); // income now = 1

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        List<String> errors = MoveValidator.validate(state, new LoanAction(card));

        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("below 0");
    }

    @Test
    void scoutDiscardsAndDraws() {
        // Use round 2 so player has 2 actions (turn won't end after 1 action)
        state.setCurrentRound(2);
        state.setActionsRemainingThisTurn(2);

        PlayerState p = state.getCurrentPlayer();

        while (!p.getHand().isEmpty()) {
            p.getHand().remove(p.getHand().getCards().getFirst());
        }
        p.getHand().add(Card.location(CityId.BIRMINGHAM));
        p.getHand().add(Card.location(CityId.WOLVERHAMPTON));
        p.getHand().add(Card.location(CityId.COVENTRY));
        p.getHand().add(Card.industry(IndustryType.COAL_MINE));
        p.getHand().add(Card.industry(IndustryType.IRON_WORKS));

        Card discard = Card.location(CityId.BIRMINGHAM);
        Card add1 = Card.location(CityId.WOLVERHAMPTON);
        Card add2 = Card.location(CityId.COVENTRY);

        ScoutAction action = new ScoutAction(discard, add1, add2);
        List<String> errors = engine.submitAction(action);

        assertThat(errors).isEmpty();
        // After scout: removed 3 cards, drew 1 → net hand = 5-3+1 = 3
        assertThat(p.getHand().size()).isEqualTo(3);
    }

    @Test
    void scoutFailsWithLessThan3Cards() {
        PlayerState p = state.getCurrentPlayer();
        // Remove cards until only 2 remain
        while (p.getHand().size() > 2) {
            p.getHand().remove(p.getHand().getCards().getLast());
        }

        Card c1 = p.getHand().getCards().get(0);
        Card c2 = p.getHand().getCards().get(1);

        List<String> errors = MoveValidator.validate(state, new ScoutAction(c1, c1, c2));
        assertThat(errors).isNotEmpty();
    }
}
