package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.Industry;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.Set;

public final class ScoringService {

    private ScoringService() {}

    public static void scoreEra(GameState state) {
        Board board = state.getBoard();

        for (PlayerState player : state.getPlayers()) {
            int vp = 0;

            for (PlacedIndustry pi : board.getPlacedIndustries()) {
                if (pi.getOwnerId() == player.getPlayerId() && pi.isFlipped()) {
                    Industry industry = Industry.create(pi.getType(), pi.getLevel());
                    vp += industry.getVictoryPoints();
                }
            }

            for (PlacedLink link : board.getPlacedLinks()) {
                if (link.getOwnerId() == player.getPlayerId()) {
                    vp += scoreLinkVP(board, link);
                }
            }

            player.addVictoryPoints(vp);
        }
    }

    private static int scoreLinkVP(Board board, PlacedLink link) {
        int vp = 0;
        vp += adjacentIndustryVP(board, link.getEdge().getCityA());
        vp += adjacentIndustryVP(board, link.getEdge().getCityB());
        return vp;
    }

    private static int adjacentIndustryVP(Board board, CityId city) {
        int count = 0;
        for (PlacedIndustry pi : board.industriesAt(city)) {
            if (pi.isFlipped()) {
                count++;
            }
        }
        return count;
    }
}
