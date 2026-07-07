package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.engine.ConnectivityService;
import hr.lknezevic.brassbirmingham.model.action.BuildAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.card.CardType;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.Industry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class BuildMoveValidator {

    private BuildMoveValidator() {}

    public static void validate(GameState state, PlayerState player, BuildAction action, List<String> errors) {
        CityId target = action.targetCity();
        IndustryType type = action.industryType();
        Card card = action.discardedCard();

        if (!player.getMat().hasAvailable(type)) {
            errors.add("No " + type + " tiles remaining on player mat");
            return;
        }

        if (!BoardDefinition.CITY_SLOTS.containsKey(target) ||
                !BoardDefinition.CITY_SLOTS.get(target).contains(type)) {
            errors.add(target + " does not accept " + type);
            return;
        }

        if (!validateCard(state, player, card, target, type, errors)) return;

        if (state.getCurrentEra() == Era.CANAL &&
                state.getBoard().countPlayerIndustriesAt(player.getPlayerId(), target) >= 1) {
            errors.add("Canal era: already have an industry in " + target);
            return;
        }

        if (!state.getBoard().hasSlotAvailable(target, type)) {
            errors.add("No available " + type + " slot in " + target);
            return;
        }

        validateCost(state, player, target, type, errors);
    }

    private static boolean validateCard(GameState state, PlayerState player, Card card,
                                        CityId target, IndustryType type, List<String> errors) {
        if (card.type() == CardType.LOCATION) {
            if (card.city() != target) {
                errors.add("Location card " + card.city() + " does not match target city " + target);
                return false;
            }
        } else if (card.type() == CardType.INDUSTRY) {
            if (card.industry() != type) {
                errors.add("Industry card " + card.industry() + " does not match industry type " + type);
                return false;
            }
            if (!ConnectivityService.isInPlayerNetwork(state, player.getPlayerId(), target) &&
                    ConnectivityService.hasAnythingOnBoard(state, player.getPlayerId())) {
                errors.add(target + " is not in your network");
                return false;
            }
        }
        return true;
    }

    private static void validateCost(GameState state, PlayerState player, CityId target,
                                     IndustryType type, List<String> errors) {
        IndustryLevel level = player.getMat().getLowestAvailable(type);
        Industry industry = Industry.create(type, level);
        int totalCost = industry.getBuildCost();
        totalCost += ResourceCostCalculator.calculateIronCost(state, industry.getIronRequired());
        totalCost += ResourceCostCalculator.calculateCoalCost(state, target, industry.getCoalRequired());

        if (player.getMoney() < totalCost) {
            errors.add("Not enough money: need £" + totalCost + ", have £" + player.getMoney());
        }
    }
}
