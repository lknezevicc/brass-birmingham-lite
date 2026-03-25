package hr.lknezevic.brassbirmingham.engine.validation;

import hr.lknezevic.brassbirmingham.engine.ConnectivityService;
import hr.lknezevic.brassbirmingham.engine.ResourceService;
import hr.lknezevic.brassbirmingham.model.action.SellAction;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.game.PlacedIndustry;
import hr.lknezevic.brassbirmingham.model.industry.Industry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.List;

public final class SellMoveValidator {

    private SellMoveValidator() {}

    public static void validate(GameState state, PlayerState player, SellAction action, List<String> errors) {
        CityId city = action.sellingCity();

        PlacedIndustry cotton = ResourceCostCalculator.findUnflippedCotton(state.getBoard(), player.getPlayerId(), city);
        if (cotton == null) {
            errors.add("No unflipped Cotton Mill owned by you at " + city);
            return;
        }

        if (!ConnectivityService.isConnectedToMerchant(state, city)) {
            errors.add(city + " is not connected to Merchant (Birmingham)");
            return;
        }

        Industry industry = Industry.create(IndustryType.COTTON_MILL, cotton.getLevel());
        int beerNeeded = industry.getBeerRequired();

        if (beerNeeded > 0) {
            int available = ResourceService.countAvailableBeer(state, player.getPlayerId(), city);
            if (available < beerNeeded) {
                errors.add("Not enough beer: need " + beerNeeded + ", available " + available);
            }
        }
    }
}
