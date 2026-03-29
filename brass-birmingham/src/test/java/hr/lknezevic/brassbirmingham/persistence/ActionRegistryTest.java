package hr.lknezevic.brassbirmingham.persistence;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.reflection.ActionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ActionRegistryTest {

    private ActionRegistry registry;

    @BeforeEach
    void setup() {
        registry = new ActionRegistry();
    }

    @Test
    void registeredTypesContainsAll5Actions() {
        List<String> types = registry.getRegisteredTypes();
        assertThat(types).containsExactlyInAnyOrder("BUILD", "NETWORK", "SELL", "LOAN", "SCOUT");
    }

    @Test
    void getActionClassReturnsCorrectClass() {
        assertThat(registry.getActionClass("BUILD")).isEqualTo(BuildAction.class);
        assertThat(registry.getActionClass("LOAN")).isEqualTo(LoanAction.class);
        assertThat(registry.getActionClass("NETWORK")).isEqualTo(NetworkAction.class);
        assertThat(registry.getActionClass("SELL")).isEqualTo(SellAction.class);
        assertThat(registry.getActionClass("SCOUT")).isEqualTo(ScoutAction.class);
    }

    @Test
    void buildActionRoundTrips() {
        Map<String, String> params = Map.of(
                "card-used", "LOCATION:BIRMINGHAM",
                "city", "BIRMINGHAM",
                "industry", "COTTON_MILL"
        );
        GameAction action = registry.fromXml("BUILD", params);
        assertThat(action).isInstanceOf(BuildAction.class);
        BuildAction build = (BuildAction) action;
        assertThat(build.targetCity()).isEqualTo(CityId.BIRMINGHAM);
        assertThat(build.industryType()).isEqualTo(IndustryType.COTTON_MILL);
    }

    @Test
    void networkActionRoundTrips() {
        Map<String, String> params = Map.of(
                "card-used", "LOCATION:WOLVERHAMPTON",
                "city-a", "DUDLEY",
                "city-b", "WOLVERHAMPTON"
        );
        GameAction action = registry.fromXml("NETWORK", params);
        assertThat(action).isInstanceOf(NetworkAction.class);
        NetworkAction net = (NetworkAction) action;
        // BoardEdge normalizes by enum ordinal: WOLVERHAMPTON(1) < DUDLEY(3)
        assertThat(net.edge().getCityA()).isEqualTo(CityId.WOLVERHAMPTON);
        assertThat(net.edge().getCityB()).isEqualTo(CityId.DUDLEY);
    }

    @Test
    void sellActionRoundTrips() {
        Map<String, String> params = Map.of(
                "card-used", "LOCATION:BIRMINGHAM",
                "city", "BIRMINGHAM",
                "selling-type", "COTTON_MILL"
        );
        GameAction action = registry.fromXml("SELL", params);
        assertThat(action).isInstanceOf(SellAction.class);
        SellAction sell = (SellAction) action;
        assertThat(sell.sellingCity()).isEqualTo(CityId.BIRMINGHAM);
        assertThat(sell.sellingType()).isEqualTo(IndustryType.COTTON_MILL);
        assertThat(sell.beerSources()).isEmpty();
    }

    @Test
    void loanActionRoundTrips() {
        Map<String, String> params = Map.of("card-used", "INDUSTRY:COAL_MINE");
        GameAction action = registry.fromXml("LOAN", params);
        assertThat(action).isInstanceOf(LoanAction.class);
        assertThat(action.discardedCard()).isEqualTo(Card.industry(IndustryType.COAL_MINE));
    }

    @Test
    void scoutActionRoundTrips() {
        Map<String, String> params = Map.of(
                "card-used", "LOCATION:COVENTRY",
                "discard1", "INDUSTRY:BREWERY",
                "discard2", "LOCATION:DUDLEY"
        );
        GameAction action = registry.fromXml("SCOUT", params);
        assertThat(action).isInstanceOf(ScoutAction.class);
        ScoutAction scout = (ScoutAction) action;
        assertThat(scout.additionalDiscard1()).isEqualTo(Card.industry(IndustryType.BREWERY));
        assertThat(scout.additionalDiscard2()).isEqualTo(Card.location(CityId.DUDLEY));
    }
}
