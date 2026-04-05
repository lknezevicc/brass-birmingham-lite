package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.BoardDefinition;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.List;

public final class BoardHitDetector {

    private BoardHitDetector() {}

    public static BoardPick pick(double x, double y) {
        for (CityId city : CityId.values()) {
            List<Rectangle2D> slots = BoardLayout.getSlotBounds(city);
            for (int i = 0; i < slots.size(); i++) {
                Rectangle2D rect = slots.get(i);
                if (rect.contains(x, y)) {
                    IndustryType type = BoardLayout.getSlotType(city, i);
                    return new BoardPick(BoardPick.Kind.SLOT, city, i, type, null);
                }
            }
        }

        for (CityId city : CityId.values()) {
            Point2D center = BoardLayout.getCityCenter(city);
            double dist = center.distance(x, y);
            if (dist <= BoardLayout.CITY_RADIUS) {
                return new BoardPick(BoardPick.Kind.CITY, city, -1, null, null);
            }
        }

        for (BoardEdge edge : BoardDefinition.EDGES) {
            Point2D mid = BoardLayout.getEdgeMidpoint(edge);
            double dist = mid.distance(x, y);
            if (dist <= BoardLayout.EDGE_HIT_RADIUS) {
                return new BoardPick(BoardPick.Kind.EDGE, null, -1, null, edge);
            }
            Point2D a = BoardLayout.getCityCenter(edge.getCityA());
            Point2D b = BoardLayout.getCityCenter(edge.getCityB());
            if (distanceToSegment(x, y, a, b) <= BoardLayout.EDGE_HIT_RADIUS) {
                return new BoardPick(BoardPick.Kind.EDGE, null, -1, null, edge);
            }
        }

        return new BoardPick(BoardPick.Kind.NONE, null, -1, null, null);
    }

    private static double distanceToSegment(double px, double py, Point2D a, Point2D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0) return a.distance(px, py);

        double t = ((px - a.getX()) * dx + (py - a.getY()) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));
        double projX = a.getX() + t * dx;
        double projY = a.getY() + t * dy;
        return Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
    }
}
