package hr.lknezevic.brassbirmingham.model.game;

import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static hr.lknezevic.brassbirmingham.model.game.CityId.*;
import static hr.lknezevic.brassbirmingham.model.industry.IndustryType.*;

public final class BoardDefinition {

    public static final List<BoardEdge> EDGES = List.of(
            new BoardEdge(WOLVERHAMPTON, DUDLEY),
            new BoardEdge(DUDLEY, BIRMINGHAM),
            new BoardEdge(BIRMINGHAM, COVENTRY),
            new BoardEdge(BIRMINGHAM, CANNOCK),
            new BoardEdge(CANNOCK, COVENTRY)
    );

    public static final Map<CityId, List<IndustryType>> CITY_SLOTS = Map.of(
            BIRMINGHAM, List.of(COTTON_MILL, BREWERY),
            WOLVERHAMPTON, List.of(COAL_MINE, BREWERY),
            COVENTRY, List.of(IRON_WORKS, BREWERY),
            DUDLEY, List.of(IRON_WORKS, COTTON_MILL),
            CANNOCK, List.of(COAL_MINE, IRON_WORKS)
    );

    public static final CityId MERCHANT_CITY = BIRMINGHAM;
    public static final Set<IndustryType> MERCHANT_ACCEPTS = Set.of(COTTON_MILL);

    private BoardDefinition() {}
}
