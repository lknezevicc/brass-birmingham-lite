package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.BoardDefinition;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class BoardLayout {

    public static final double CANVAS_WIDTH = 700;
    public static final double CANVAS_HEIGHT = 500;
    public static final double CITY_RADIUS = 30;
    public static final double SLOT_WIDTH = 50;
    public static final double SLOT_HEIGHT = 36;
    public static final double EDGE_HIT_RADIUS = 18;

    private static final Map<CityId, Point2D> CITY_POSITIONS = new EnumMap<>(CityId.class);
    private static final Map<CityId, List<Rectangle2D>> SLOT_BOUNDS = new EnumMap<>(CityId.class);

    static {
        CITY_POSITIONS.put(CityId.BIRMINGHAM, new Point2D(350, 250));
        CITY_POSITIONS.put(CityId.WOLVERHAMPTON, new Point2D(150, 120));
        CITY_POSITIONS.put(CityId.DUDLEY, new Point2D(150, 300));
        CITY_POSITIONS.put(CityId.COVENTRY, new Point2D(550, 350));
        CITY_POSITIONS.put(CityId.CANNOCK, new Point2D(500, 130));

        for (CityId city : CityId.values()) {
            Point2D center = CITY_POSITIONS.get(city);
            double slotY = center.getY() + CITY_RADIUS + 8;
            Rectangle2D slot0 = new Rectangle2D(
                    center.getX() - SLOT_WIDTH - 4, slotY, SLOT_WIDTH, SLOT_HEIGHT);
            Rectangle2D slot1 = new Rectangle2D(
                    center.getX() + 4, slotY, SLOT_WIDTH, SLOT_HEIGHT);
            SLOT_BOUNDS.put(city, List.of(slot0, slot1));
        }
    }

    private BoardLayout() {}

    public static Point2D getCityCenter(CityId city) {
        return CITY_POSITIONS.get(city);
    }

    public static Map<CityId, Point2D> getAllCityPositions() {
        return Map.copyOf(CITY_POSITIONS);
    }

    public static List<Rectangle2D> getSlotBounds(CityId city) {
        return SLOT_BOUNDS.get(city);
    }

    public static IndustryType getSlotType(CityId city, int slotIndex) {
        List<IndustryType> slots = BoardDefinition.CITY_SLOTS.get(city);
        if (slots == null || slotIndex < 0 || slotIndex >= slots.size()) return null;
        return slots.get(slotIndex);
    }

    public static Point2D getEdgeMidpoint(BoardEdge edge) {
        Point2D a = CITY_POSITIONS.get(edge.getCityA());
        Point2D b = CITY_POSITIONS.get(edge.getCityB());
        return a.midpoint(b);
    }

    public static Map<CityId, List<Rectangle2D>> getAllSlotBounds() {
        return Map.copyOf(SLOT_BOUNDS);
    }
}
