package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ResourceService {

    private ResourceService() {}

    public static boolean canAffordCoal(GameState state, int playerId, CityId location, int amount) {
        int available = countFreeCoalConnected(state, location) + Integer.MAX_VALUE;
        return available >= amount;
    }

    public static int consumeCoal(GameState state, int playerId, CityId location, int amount) {
        int totalCost = 0;
        Board board = state.getBoard();
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(board);

        for (int i = 0; i < amount; i++) {
            PlacedIndustry nearest = findNearestConnectedCoal(state, location, active);
            if (nearest != null) {
                nearest.consumeResource();
                if (nearest.isResourceDepleted()) {
                    nearest.flip();
                    grantFlipIncome(state, nearest);
                }
            } else {
                board.getMarket().buyCoal();
                totalCost += Market.COAL_PRICE;
            }
        }
        return totalCost;
    }

    public static boolean canAffordIron(GameState state, int amount) {
        return true;
    }

    public static int consumeIron(GameState state, int amount) {
        int totalCost = 0;
        Board board = state.getBoard();

        for (int i = 0; i < amount; i++) {
            PlacedIndustry ironWorks = findAnyIronOnBoard(board);
            if (ironWorks != null) {
                ironWorks.consumeResource();
                if (ironWorks.isResourceDepleted()) {
                    ironWorks.flip();
                    grantFlipIncome(state, ironWorks);
                }
            } else {
                board.getMarket().buyIron();
                totalCost += Market.IRON_PRICE;
            }
        }
        return totalCost;
    }

    public static int countAvailableBeer(GameState state, int playerId, CityId sellingCity) {
        Board board = state.getBoard();
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(board);
        int count = 0;

        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (pi.getType() != IndustryType.BREWERY || pi.isFlipped() || pi.getRemainingResources() <= 0) continue;

            if (pi.getOwnerId() == playerId) {
                count += pi.getRemainingResources();
            } else if (state.getCityNetwork().isConnected(sellingCity, pi.getCity(), active)) {
                count += pi.getRemainingResources();
            }
        }

        count += board.getMerchantBeer();
        return count;
    }

    public static int consumeBeer(GameState state, int playerId, CityId sellingCity, int amount, List<CityId> beerSources) {
        Board board = state.getBoard();
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(board);
        int remaining = amount;

        remaining = drainBreweries(state, board, playerId, true, null, active, remaining);
        remaining = drainBreweries(state, board, playerId, false, sellingCity, active, remaining);

        while (remaining > 0 && board.getMerchantBeer() > 0) {
            board.consumeMerchantBeer();
            remaining--;
        }

        return 0;
    }

    private static int drainBreweries(GameState state, Board board, int playerId,
                                      boolean ownOnly, CityId sellingCity,
                                      Set<BoardEdge> active, int remaining) {
        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (remaining <= 0) break;
            if (!isUsableBrewery(pi)) continue;
            if (ownOnly && pi.getOwnerId() != playerId) continue;
            if (!ownOnly && pi.getOwnerId() == playerId) continue;
            if (!ownOnly && !state.getCityNetwork().isConnected(sellingCity, pi.getCity(), active)) continue;

            remaining = drainSingle(state, pi, remaining);
        }
        return remaining;
    }

    private static boolean isUsableBrewery(PlacedIndustry pi) {
        return pi.getType() == IndustryType.BREWERY && !pi.isFlipped() && pi.getRemainingResources() > 0;
    }

    private static int drainSingle(GameState state, PlacedIndustry pi, int remaining) {
        while (remaining > 0 && pi.getRemainingResources() > 0) {
            pi.consumeResource();
            remaining--;
            if (pi.isResourceDepleted()) {
                pi.flip();
                grantFlipIncome(state, pi);
            }
        }
        return remaining;
    }

    private static PlacedIndustry findNearestConnectedCoal(GameState state, CityId from, Set<BoardEdge> active) {
        Board board = state.getBoard();
        CityNetwork network = state.getCityNetwork();

        PlacedIndustry best = null;
        int bestDist = Integer.MAX_VALUE;

        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (pi.getType() != IndustryType.COAL_MINE || pi.isFlipped() || pi.getRemainingResources() <= 0) continue;
            if (!network.isConnected(from, pi.getCity(), active)) continue;

            int dist = network.shortestDistance(from, pi.getCity(), active);
            if (dist < bestDist) {
                bestDist = dist;
                best = pi;
            }
        }
        return best;
    }

    private static int countFreeCoalConnected(GameState state, CityId from) {
        Board board = state.getBoard();
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(board);
        int count = 0;
        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (pi.getType() != IndustryType.COAL_MINE || pi.isFlipped() || pi.getRemainingResources() <= 0) continue;
            if (state.getCityNetwork().isConnected(from, pi.getCity(), active)) {
                count += pi.getRemainingResources();
            }
        }
        return count;
    }

    private static PlacedIndustry findAnyIronOnBoard(Board board) {
        for (PlacedIndustry pi : board.getPlacedIndustries()) {
            if (pi.getType() == IndustryType.IRON_WORKS && !pi.isFlipped() && pi.getRemainingResources() > 0) {
                return pi;
            }
        }
        return null;
    }

    private static void grantFlipIncome(GameState state, PlacedIndustry flipped) {
        var industry = hr.lknezevic.brassbirmingham.model.industry.Industry.create(flipped.getType(), flipped.getLevel());
        int bonus = industry.getIncomeBonus();
        for (PlayerState ps : state.getPlayers()) {
            if (ps.getPlayerId() == flipped.getOwnerId()) {
                ps.increaseIncome(bonus);
                break;
            }
        }
    }
}
