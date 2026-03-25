package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.model.game.Board;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.game.Market;
import hr.lknezevic.brassbirmingham.model.game.PlacedIndustry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.util.Set;

public final class ResourceCostCalculator {

    private ResourceCostCalculator() {}

    public static int calculateIronCost(GameState state, int amount) {
        int freeIron = 0;
        for (PlacedIndustry pi : state.getBoard().getPlacedIndustries()) {
            if (pi.getType() == IndustryType.IRON_WORKS && !pi.isFlipped()) {
                freeIron += pi.getRemainingResources();
            }
        }
        int needFromMarket = Math.max(0, amount - freeIron);
        return needFromMarket * Market.IRON_PRICE;
    }

    public static int calculateCoalCost(GameState state, CityId location, int amount) {
        Board board = state.getBoard();
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(board);
        int freeCoal = 0;
        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (pi.getType() == IndustryType.COAL_MINE && !pi.isFlipped() && pi.getRemainingResources() > 0) {
                if (state.getCityNetwork().isConnected(location, pi.getCity(), active)) {
                    freeCoal += pi.getRemainingResources();
                }
            }
        }
        int needFromMarket = Math.max(0, amount - freeCoal);
        return needFromMarket * Market.COAL_PRICE;
    }

    public static PlacedIndustry findUnflippedCotton(Board board, int playerId, CityId city) {
        return board.industriesAt(city).stream()
                .filter(p -> p.getOwnerId() == playerId
                        && p.getType() == IndustryType.COTTON_MILL
                        && !p.isFlipped())
                .findFirst()
                .orElse(null);
    }
}
