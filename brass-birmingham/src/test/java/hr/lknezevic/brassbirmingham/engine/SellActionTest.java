package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.SellAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SellActionTest {

    private GameState state;
    private RulesEngine engine;

    @BeforeEach
    void setup() {
        state = GameStateFactory.newGame("Alice", "Bob");
        engine = new RulesEngine(state);

        // Place an unflipped Cotton Mill L1 at Birmingham for P0
        state.getBoard().addIndustry(
                new PlacedIndustry(0, CityId.BIRMINGHAM, IndustryType.COTTON_MILL, IndustryLevel.L1, 0));

        // Birmingham IS the merchant, so it's connected to merchant by definition (distance 0)
        // Place a link so connectivity works
        state.getBoard().addLink(new PlacedLink(0,
                new BoardEdge(CityId.BIRMINGHAM, CityId.DUDLEY), LinkType.CANAL));
    }

    @Test
    void sellCottonL1NeedNoBeer() {
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        SellAction action = new SellAction(card, CityId.BIRMINGHAM, IndustryType.COTTON_MILL, List.of());

        List<String> errors = engine.submitAction(action);
        assertThat(errors).isEmpty();

        PlacedIndustry cotton = state.getBoard().industriesAt(CityId.BIRMINGHAM).stream()
                .filter(p -> p.getType() == IndustryType.COTTON_MILL)
                .findFirst().orElseThrow();
        assertThat(cotton.isFlipped()).isTrue();
    }

    @Test
    void sellIncreasesIncome() {
        int incomeBefore = state.getCurrentPlayer().getIncomeLevel();
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        engine.submitAction(new SellAction(card, CityId.BIRMINGHAM, IndustryType.COTTON_MILL, List.of()));

        assertThat(state.getPlayers().get(0).getIncomeLevel()).isGreaterThan(incomeBefore);
    }

    @Test
    void sellFailsIfNotConnectedToMerchant() {
        // Place cotton at Wolverhampton with no links connecting to Birmingham
        state.getBoard().getPlacedLinks().clear();
        state.getBoard().addIndustry(
                new PlacedIndustry(0, CityId.WOLVERHAMPTON, IndustryType.COTTON_MILL, IndustryLevel.L1, 0));

        state.setActionsRemainingThisTurn(2);
        state.setCurrentRound(2);

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        List<String> errors = MoveValidator.validate(state,
                new SellAction(card, CityId.WOLVERHAMPTON, IndustryType.COTTON_MILL, List.of()));

        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("not connected to Merchant");
    }

    @Test
    void sellCottonL2RequiresBeer() {
        // Replace with L2 cotton
        state.getBoard().getPlacedIndustries().clear();
        state.getBoard().addIndustry(
                new PlacedIndustry(0, CityId.BIRMINGHAM, IndustryType.COTTON_MILL, IndustryLevel.L2, 0));

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        // No beer sources available (merchant beer = 1, but let's verify it works)
        SellAction action = new SellAction(card, CityId.BIRMINGHAM, IndustryType.COTTON_MILL, List.of());
        List<String> errors = engine.submitAction(action);

        // Should succeed — merchant has 1 beer
        assertThat(errors).isEmpty();
        assertThat(state.getBoard().getMerchantBeer()).isEqualTo(0);
    }
}
