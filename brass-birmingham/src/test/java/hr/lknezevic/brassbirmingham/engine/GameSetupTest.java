package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.card.CardType;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameSetupTest {

    private GameState state;

    @BeforeEach
    void setup() {
        state = GameStateFactory.newGame("Alice", "Bob");
    }

    @Test
    void newGameHasTwoPlayers() {
        assertThat(state.getPlayers()).hasSize(2);
        assertThat(state.getPlayers().get(0).getName()).isEqualTo("Alice");
        assertThat(state.getPlayers().get(1).getName()).isEqualTo("Bob");
    }

    @Test
    void eachPlayerStartsWith5Cards() {
        for (PlayerState p : state.getPlayers()) {
            assertThat(p.getHand().size()).isEqualTo(5);
        }
    }

    @Test
    void eachPlayerStartsWith17Money() {
        for (PlayerState p : state.getPlayers()) {
            assertThat(p.getMoney()).isEqualTo(17);
        }
    }

    @Test
    void startsInCanalEraRound1() {
        assertThat(state.getCurrentEra()).isEqualTo(Era.CANAL);
        assertThat(state.getCurrentRound()).isEqualTo(1);
    }

    @Test
    void firstRoundOfCanalHas1Action() {
        assertThat(state.getActionsRemainingThisTurn()).isEqualTo(1);
    }

    @Test
    void marketStartsFullyStocked() {
        assertThat(state.getBoard().getMarket().getCoalSupply()).isEqualTo(4);
        assertThat(state.getBoard().getMarket().getIronSupply()).isEqualTo(4);
    }

    @Test
    void merchantBeerStartsAt1() {
        assertThat(state.getBoard().getMerchantBeer()).isEqualTo(1);
    }

    @Test
    void deckHasCorrectSizeAfterDealing() {
        // 18 cards total - 10 dealt (5 each) = 8 remaining
        assertThat(state.getDeck().drawPileSize()).isEqualTo(8);
    }
}
