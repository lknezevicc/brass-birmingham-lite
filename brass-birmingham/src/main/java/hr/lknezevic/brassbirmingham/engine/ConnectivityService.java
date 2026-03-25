package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.Set;

public final class ConnectivityService {

    private ConnectivityService() {}

    public static boolean isInPlayerNetwork(GameState state, int playerId, CityId city) {
        Set<CityId> network = state.getCityNetwork().playerNetwork(playerId, state.getBoard());
        return network.contains(city);
    }

    public static boolean hasAnythingOnBoard(GameState state, int playerId) {
        Board board = state.getBoard();
        boolean hasIndustry = board.getPlacedIndustries().stream()
                .anyMatch(p -> p.getOwnerId() == playerId);
        boolean hasLink = board.getPlacedLinks().stream()
                .anyMatch(l -> l.getOwnerId() == playerId);
        return hasIndustry || hasLink;
    }

    public static boolean isConnectedToMerchant(GameState state, CityId from) {
        Set<BoardEdge> active = state.getCityNetwork().activeLinks(state.getBoard());
        return state.getCityNetwork().isConnected(from, BoardDefinition.MERCHANT_CITY, active);
    }

    public static boolean isEdgeAdjacentToNetwork(GameState state, int playerId, BoardEdge edge) {
        Set<CityId> network = state.getCityNetwork().playerNetwork(playerId, state.getBoard());
        return network.contains(edge.getCityA()) || network.contains(edge.getCityB());
    }
}
