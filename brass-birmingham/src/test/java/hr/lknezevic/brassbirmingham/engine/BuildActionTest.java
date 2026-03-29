package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuildActionTest {

    private GameState state;
    private RulesEngine engine;

    @BeforeEach
    void setup() {
        state = GameStateFactory.newGame("Alice", "Bob");
        engine = new RulesEngine(state);
    }

    @Test
    void buildWithLocationCardSucceeds() {
        Card locationCard = Card.location(CityId.BIRMINGHAM);
        ensureInHand(0, locationCard);

        BuildAction action = new BuildAction(locationCard, CityId.BIRMINGHAM, IndustryType.COTTON_MILL);
        List<String> errors = engine.submitAction(action);

        assertThat(errors).isEmpty();
        assertThat(state.getBoard().industriesAt(CityId.BIRMINGHAM)).hasSize(1);
    }

    @Test
    void buildWithIndustryCardRequiresNetwork() {
        Card industryCard = Card.industry(IndustryType.COAL_MINE);
        ensureInHand(0, industryCard);

        // Player has nothing on board -> should be allowed anywhere (first placement rule)
        BuildAction action = new BuildAction(industryCard, CityId.WOLVERHAMPTON, IndustryType.COAL_MINE);
        List<String> errors = engine.submitAction(action);

        assertThat(errors).isEmpty();
    }

    @Test
    void buildWithIndustryCardFailsIfNotInNetworkAndHasPresence() {
        // First, place something for P0 in Birmingham
        placeIndustryDirectly(0, CityId.BIRMINGHAM, IndustryType.COAL_MINE);

        // Now advance to P0's turn with 2 actions (round 2)
        state.setCurrentRound(2);
        state.setActionsRemainingThisTurn(2);
        state.setCurrentPlayerIndex(0);

        Card industryCard = Card.industry(IndustryType.BREWERY);
        ensureInHand(0, industryCard);

        // Wolverhampton is not in P0's network (no link to Birmingham)
        BuildAction action = new BuildAction(industryCard, CityId.WOLVERHAMPTON, IndustryType.BREWERY);
        List<String> errors = MoveValidator.validate(state, action);

        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("not in your network");
    }

    @Test
    void buildDeductsMoneyAndPlacesTile() {
        Card locationCard = Card.location(CityId.CANNOCK);
        ensureInHand(0, locationCard);

        int moneyBefore = state.getCurrentPlayer().getMoney();
        BuildAction action = new BuildAction(locationCard, CityId.CANNOCK, IndustryType.COAL_MINE);
        engine.submitAction(action);

        assertThat(state.getPlayers().get(0).getMoney()).isLessThan(moneyBefore);
        assertThat(state.getBoard().industriesAt(CityId.CANNOCK)).hasSize(1);
    }

    @Test
    void buildCoalMinePlacesResources() {
        Card locationCard = Card.location(CityId.WOLVERHAMPTON);
        ensureInHand(0, locationCard);

        BuildAction action = new BuildAction(locationCard, CityId.WOLVERHAMPTON, IndustryType.COAL_MINE);
        engine.submitAction(action);

        PlacedIndustry placed = state.getBoard().industriesAt(CityId.WOLVERHAMPTON).getFirst();
        assertThat(placed.getRemainingResources()).isEqualTo(2); // L1 coal mine has 2 resources
    }

    @Test
    void canalEraLimitsOneIndustryPerPlayerPerCity() {
        Card card1 = Card.location(CityId.WOLVERHAMPTON);
        ensureInHand(0, card1);
        engine.submitAction(new BuildAction(card1, CityId.WOLVERHAMPTON, IndustryType.COAL_MINE));

        // Now advance state so P0 can act again
        state.setActionsRemainingThisTurn(2);
        state.setCurrentPlayerIndex(0);
        state.setCurrentRound(2);

        Card card2 = Card.location(CityId.WOLVERHAMPTON);
        ensureInHand(0, card2);
        List<String> errors = MoveValidator.validate(state,
                new BuildAction(card2, CityId.WOLVERHAMPTON, IndustryType.BREWERY));

        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("Canal era");
    }

    @Test
    void buildFailsForWrongLocationType() {
        // Wolverhampton accepts COAL_MINE and BREWERY, not IRON_WORKS
        Card card = Card.location(CityId.WOLVERHAMPTON);
        ensureInHand(0, card);

        List<String> errors = MoveValidator.validate(state,
                new BuildAction(card, CityId.WOLVERHAMPTON, IndustryType.IRON_WORKS));

        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("does not accept");
    }

    private void ensureInHand(int playerId, Card card) {
        PlayerState p = state.getPlayers().get(playerId);
        if (!p.getHand().contains(card)) {
            if (p.getHand().size() >= 5) {
                p.getHand().remove(p.getHand().getCards().getFirst());
            }
            p.getHand().add(card);
        }
    }

    private void placeIndustryDirectly(int playerId, CityId city, IndustryType type) {
        state.getBoard().addIndustry(
                new PlacedIndustry(playerId, city, type, IndustryLevel.L1, 2));
    }
}
