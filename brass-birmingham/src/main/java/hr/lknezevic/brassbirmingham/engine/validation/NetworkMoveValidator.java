package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.engine.ConnectivityService;
import hr.lknezevic.brassbirmingham.model.action.NetworkAction;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class NetworkMoveValidator {

    private NetworkMoveValidator() {}

    public static void validate(GameState state, PlayerState player, NetworkAction action, List<String> errors) {
        BoardEdge edge = action.edge();
        Era era = state.getCurrentEra();

        if (state.getBoard().isEdgeOccupied(edge)) {
            errors.add("Edge already has a link");
            return;
        }

        if (!BoardDefinition.EDGES.contains(edge)) {
            errors.add("Invalid edge");
            return;
        }

        if (!player.hasLinksRemaining(era)) {
            errors.add("No " + (era == Era.CANAL ? "canal" : "rail") + " links remaining");
            return;
        }

        if (ConnectivityService.hasAnythingOnBoard(state, player.getPlayerId())) {
            if (!ConnectivityService.isEdgeAdjacentToNetwork(state, player.getPlayerId(), edge)) {
                errors.add("Edge not adjacent to your network");
                return;
            }
        }

        int cost = era == Era.CANAL ? 3 : 5;
        int coalCost = era == Era.RAIL ? Market.COAL_PRICE : 0;

        if (player.getMoney() < cost + (era == Era.RAIL ? coalCost : 0)) {
            errors.add("Not enough money for network action");
        }
    }
}
