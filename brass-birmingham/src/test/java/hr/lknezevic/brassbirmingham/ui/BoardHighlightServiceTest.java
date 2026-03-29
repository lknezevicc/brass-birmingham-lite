package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.card.CardType;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BoardHighlightServiceTest {

    @Test
    void validBuildSlotsNonEmptyForNewGame() {
        GameState state = GameStateFactory.newGame("A", "B");
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        Set<SlotTarget> valid = BoardHighlightService.getValidBuildSlots(state, card);
        assertThat(valid).isNotEmpty();
    }

    @Test
    void locationCardRestrictsToMatchingCity() {
        GameState state = GameStateFactory.newGame("A", "B");
        Card locationCard = findLocationCard(state);
        if (locationCard == null) return; // might not draw one — skip

        Set<SlotTarget> valid = BoardHighlightService.getValidBuildSlots(state, locationCard);
        for (SlotTarget t : valid) {
            assertThat(t.city()).isEqualTo(locationCard.city());
        }
    }

    @Test
    void validNetworkEdgesReturnsFreeEdges() {
        GameState state = GameStateFactory.newGame("A", "B");
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        Set<BoardEdge> valid = BoardHighlightService.getValidNetworkEdges(state, card);
        // In canal era, player needs links remaining — should have some valid edges
        assertThat(valid).isNotEmpty();
    }

    @Test
    void nullCardReturnsEmptyHighlights() {
        GameState state = GameStateFactory.newGame("A", "B");
        assertThat(BoardHighlightService.getValidBuildSlots(state, null)).isEmpty();
        assertThat(BoardHighlightService.getValidNetworkEdges(state, null)).isEmpty();
        assertThat(BoardHighlightService.getValidSellCities(state, null)).isEmpty();
    }

    private Card findLocationCard(GameState state) {
        return state.getCurrentPlayer().getHand().getCards().stream()
                .filter(c -> c.type() == CardType.LOCATION)
                .findFirst().orElse(null);
    }
}
