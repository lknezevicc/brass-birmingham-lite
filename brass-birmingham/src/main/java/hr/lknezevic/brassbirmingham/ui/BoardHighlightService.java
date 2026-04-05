package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.engine.MoveValidator;
import hr.lknezevic.brassbirmingham.model.action.BuildAction;
import hr.lknezevic.brassbirmingham.model.action.NetworkAction;
import hr.lknezevic.brassbirmingham.model.action.SellAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.BoardDefinition;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.util.*;

// probe MoveValidator instead of duplicating rule logic
public final class BoardHighlightService {

    private BoardHighlightService() {}

    public static Set<SlotTarget> getValidBuildSlots(GameState state, Card card) {
        if (card == null) return Set.of();
        Set<SlotTarget> valid = new HashSet<>();
        for (CityId city : CityId.values()) {
            List<IndustryType> slots = BoardDefinition.CITY_SLOTS.get(city);
            if (slots == null) continue;
            for (int i = 0; i < slots.size(); i++) {
                IndustryType type = slots.get(i);
                BuildAction probe = new BuildAction(card, city, type);
                List<String> errors = MoveValidator.validate(state, probe);
                if (errors.isEmpty()) {
                    valid.add(new SlotTarget(city, i, type));
                }
            }
        }
        return valid;
    }

    public static Set<BoardEdge> getValidNetworkEdges(GameState state, Card card) {
        if (card == null) return Set.of();
        Set<BoardEdge> valid = new HashSet<>();
        for (BoardEdge edge : BoardDefinition.EDGES) {
            NetworkAction probe = new NetworkAction(card, edge);
            List<String> errors = MoveValidator.validate(state, probe);
            if (errors.isEmpty()) {
                valid.add(edge);
            }
        }
        return valid;
    }

    public static Set<CityId> getValidSellCities(GameState state, Card card) {
        if (card == null) return Set.of();
        Set<CityId> valid = new HashSet<>();
        for (CityId city : CityId.values()) {
            SellAction probe = new SellAction(card, city, IndustryType.COTTON_MILL, List.of());
            List<String> errors = MoveValidator.validate(state, probe);
            if (errors.isEmpty()) {
                valid.add(city);
            }
        }
        return valid;
    }
}
