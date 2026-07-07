package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.Industry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class ActionExecutor {

    private ActionExecutor() {}

    public static void execute(GameState state, GameAction action) {
        PlayerState player = state.getCurrentPlayer();
        GameFlowLogger.entering("action={}, player={}, money={}", action.getClass().getSimpleName(), player.getPlayerId(), player.getMoney());

        player.getHand().remove(action.discardedCard());
        state.getDeck().discard(action.discardedCard());

        switch (action) {
            case BuildAction build -> executeBuild(state, player, build);
            case NetworkAction network -> executeNetwork(state, player, network);
            case SellAction sell -> executeSell(state, player, sell);
            case LoanAction loan -> executeLoan(state, player, loan);
            case ScoutAction scout -> executeScout(state, player, scout);
            default -> throw new IllegalStateException("Unknown action: " + action.getClass());
        }

        state.decrementActions();
        GameFlowLogger.exiting("money={}, income={}, vp={}", player.getMoney(), player.getIncomeLevel(), player.getVictoryPoints());
    }

    private static void executeBuild(GameState state, PlayerState player, BuildAction action) {
        GameFlowLogger.event("BUILD: city={}, type={}", action.targetCity(), action.industryType());
        IndustryType type = action.industryType();
        IndustryLevel level = player.getMat().getLowestAvailable(type);
        Industry industry = Industry.create(type, level);

        int buildCost = industry.getBuildCost();
        int ironCost = ResourceService.consumeIron(state, industry.getIronRequired());
        int coalCost = ResourceService.consumeCoal(state, player.getPlayerId(), action.targetCity(), industry.getCoalRequired());
        player.spend(buildCost + ironCost + coalCost);

        player.getMat().removeTile(type, level);

        int resources = industry.getResourceCapacity();
        PlacedIndustry placed = new PlacedIndustry(
                player.getPlayerId(), action.targetCity(), type, level, resources
        );
        state.getBoard().addIndustry(placed);

        if (resources == 0 && industry.getFlipTrigger() == hr.lknezevic.brassbirmingham.model.industry.FlipTrigger.RESOURCE_DEPLETION) {
            placed.flip();
            player.increaseIncome(industry.getIncomeBonus());
        }
    }

    private static void executeNetwork(GameState state, PlayerState player, NetworkAction action) {
        GameFlowLogger.event("NETWORK: edge={}, era={}", action.edge(), state.getCurrentEra());
        Era era = state.getCurrentEra();
        LinkType linkType = era == Era.CANAL ? LinkType.CANAL : LinkType.RAIL;
        int cost = era == Era.CANAL ? 3 : 5;

        int coalCost = 0;
        if (era == Era.RAIL) {
            coalCost = ResourceService.consumeCoal(state, player.getPlayerId(), action.edge().getCityA(), 1);
        }

        player.spend(cost + coalCost);

        if (era == Era.CANAL) {
            player.useCanalLink();
        } else {
            player.useRailLink();
        }

        PlacedLink link = new PlacedLink(player.getPlayerId(), action.edge(), linkType);
        state.getBoard().addLink(link);
    }

    private static void executeSell(GameState state, PlayerState player, SellAction action) {
        GameFlowLogger.event("SELL: city={}", action.sellingCity());
        Board board = state.getBoard();
        PlacedIndustry cotton = board.industriesAt(action.sellingCity()).stream()
                .filter(p -> p.getOwnerId() == player.getPlayerId()
                        && p.getType() == IndustryType.COTTON_MILL
                        && !p.isFlipped())
                .findFirst()
                .orElseThrow();

        Industry industry = Industry.create(IndustryType.COTTON_MILL, cotton.getLevel());
        int beerNeeded = industry.getBeerRequired();

        if (beerNeeded > 0) {
            ResourceService.consumeBeer(state, player.getPlayerId(), action.sellingCity(), beerNeeded, action.beerSources());
        }

        cotton.flip();
        player.increaseIncome(industry.getIncomeBonus());
    }

    private static void executeLoan(GameState state, PlayerState player, LoanAction action) {
        int incomeBefore = player.getIncomeLevel();
        player.earn(20);
        player.decreaseIncomeLevel(2);
        GameFlowLogger.stateChange("income", incomeBefore, player.getIncomeLevel());
    }

    private static void executeScout(GameState state, PlayerState player, ScoutAction action) {
        GameFlowLogger.event("SCOUT: discarding 2 extra, drawing 1");
        player.getHand().remove(action.additionalDiscard1());
        player.getHand().remove(action.additionalDiscard2());
        state.getDeck().discard(action.additionalDiscard1());
        state.getDeck().discard(action.additionalDiscard2());

        var card = state.getDeck().draw();
        if (card != null) {
            player.getHand().add(card);
        }
    }
}
