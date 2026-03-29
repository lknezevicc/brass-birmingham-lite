package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.action.NetworkAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkActionTest {

    private GameState state;
    private RulesEngine engine;

    @BeforeEach
    void setup() {
        state = GameStateFactory.newGame("Alice", "Bob");
        engine = new RulesEngine(state);
    }

    @Test
    void buildCanalLinkCosts3() {
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        BoardEdge edge = BoardDefinition.EDGES.getFirst();

        int moneyBefore = state.getCurrentPlayer().getMoney();
        NetworkAction action = new NetworkAction(card, edge);
        engine.submitAction(action);

        assertThat(state.getPlayers().get(0).getMoney()).isEqualTo(moneyBefore - 3);
    }

    @Test
    void canalLinkPlacedOnBoard() {
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        BoardEdge edge = BoardDefinition.EDGES.getFirst();

        engine.submitAction(new NetworkAction(card, edge));

        assertThat(state.getBoard().getPlacedLinks()).hasSize(1);
        assertThat(state.getBoard().getPlacedLinks().getFirst().getLinkType()).isEqualTo(LinkType.CANAL);
    }

    @Test
    void cannotBuildOnOccupiedEdge() {
        Card card1 = state.getCurrentPlayer().getHand().getCards().getFirst();
        BoardEdge edge = BoardDefinition.EDGES.getFirst();
        engine.submitAction(new NetworkAction(card1, edge));

        // Move to P1 and try same edge
        state.setCurrentPlayerIndex(1);
        state.setActionsRemainingThisTurn(2);
        Card card2 = state.getPlayers().get(1).getHand().getCards().getFirst();

        List<String> errors = MoveValidator.validate(state, new NetworkAction(card2, edge));
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("already has a link");
    }

    @Test
    void railLinkCosts5PlusCoal() {
        state.setCurrentEra(Era.RAIL);
        state.setActionsRemainingThisTurn(2);

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        BoardEdge edge = BoardDefinition.EDGES.getFirst();

        int moneyBefore = state.getCurrentPlayer().getMoney();
        engine.submitAction(new NetworkAction(card, edge));

        // 5 for rail + 5 for coal from market (no coal mine connected)
        assertThat(state.getPlayers().get(0).getMoney()).isEqualTo(moneyBefore - 10);
    }

    @Test
    void networkWithNothingOnBoardCanPlaceAnywhere() {
        // Fresh game, P0 has nothing on board — should be able to place on any edge
        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();

        for (BoardEdge edge : BoardDefinition.EDGES) {
            List<String> errors = MoveValidator.validate(state, new NetworkAction(card, edge));
            assertThat(errors).isEmpty();
        }
    }
}
