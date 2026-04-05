package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Set;

public final class BoardRenderer {

    private final Canvas canvas;

    public BoardRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(GameState state) {
        render(state, Set.of(), Set.of(), Set.of());
    }

    public void render(GameState state,
                       Set<CityId> highlightCities,
                       Set<BoardEdge> highlightEdges,
                       Set<SlotTarget> highlightSlots) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);
        drawBackground(gc, w, h);
        drawEdges(gc, state, highlightEdges);
        drawCities(gc, highlightCities);
        drawIndustrySlots(gc, state, highlightSlots);
        drawMarketHud(gc, state);
    }

    private void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(BoardColors.BG_CANAL_DEEP);
        gc.fillRect(0, 0, w, h);

        gc.setStroke(BoardColors.BORDER_SOFT);
        gc.setLineWidth(1);
        for (int x = 0; x < w; x += 50) {
            gc.strokeLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 50) {
            gc.strokeLine(0, y, w, y);
        }
    }

    private void drawEdges(GraphicsContext gc, GameState state, Set<BoardEdge> highlights) {
        for (BoardEdge edge : BoardDefinition.EDGES) {
            Point2D a = BoardLayout.getCityCenter(edge.getCityA());
            Point2D b = BoardLayout.getCityCenter(edge.getCityB());

            PlacedLink link = findLink(state, edge);
            boolean highlighted = highlights.contains(edge);

            if (link != null) {
                gc.setStroke(link.getLinkType() == LinkType.CANAL
                        ? BoardColors.CANAL_LINK : BoardColors.RAIL_LINK);
                gc.setLineWidth(5);
                gc.setLineDashes();
            } else if (highlighted) {
                gc.setStroke(BoardColors.HIGHLIGHT_STROKE);
                gc.setLineWidth(4);
                gc.setLineDashes();
            } else {
                gc.setStroke(BoardColors.BORDER_SOFT);
                gc.setLineWidth(2);
                gc.setLineDashes(8, 6);
            }
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());

            if (link != null) {
                Point2D mid = a.midpoint(b);
                Color ownerColor = link.getOwnerId() == 0 ? BoardColors.PLAYER_1 : BoardColors.PLAYER_2;
                gc.setFill(ownerColor);
                gc.fillOval(mid.getX() - 6, mid.getY() - 6, 12, 12);
            }
        }
        gc.setLineDashes();
    }

    private void drawCities(GraphicsContext gc, Set<CityId> highlights) {
        gc.setFont(Font.font("System", 11));
        gc.setTextAlign(TextAlignment.CENTER);

        for (CityId city : CityId.values()) {
            Point2D center = BoardLayout.getCityCenter(city);
            double r = BoardLayout.CITY_RADIUS;

            boolean highlighted = highlights.contains(city);

            gc.setFill(BoardColors.SURFACE_3);
            gc.fillOval(center.getX() - r, center.getY() - r, r * 2, r * 2);

            gc.setStroke(highlighted ? BoardColors.HIGHLIGHT_STROKE : BoardColors.BORDER_STRONG);
            gc.setLineWidth(highlighted ? 3 : 2);
            gc.strokeOval(center.getX() - r, center.getY() - r, r * 2, r * 2);

            gc.setFill(BoardColors.TEXT_PRIMARY);
            gc.fillText(BoardDrawHelper.formatCityName(city), center.getX(), center.getY() + 4);
        }
    }

    private void drawIndustrySlots(GraphicsContext gc, GameState state, Set<SlotTarget> highlights) {
        gc.setFont(Font.font("System", 9));
        gc.setTextAlign(TextAlignment.CENTER);

        for (CityId city : CityId.values()) {
            List<Rectangle2D> slots = BoardLayout.getSlotBounds(city);
            List<IndustryType> slotTypes = BoardDefinition.CITY_SLOTS.get(city);

            for (int i = 0; i < slots.size(); i++) {
                Rectangle2D rect = slots.get(i);
                IndustryType slotType = slotTypes.get(i);
                PlacedIndustry placed = findPlacedAt(state, city, slotType);
                SlotTarget target = new SlotTarget(city, i, slotType);
                boolean highlighted = highlights.contains(target);

                if (placed != null) {
                    BoardDrawHelper.drawPlacedTile(gc, rect, placed);
                } else {
                    BoardDrawHelper.drawEmptySlot(gc, rect, slotType, highlighted);
                }
            }
        }
    }

    private void drawMarketHud(GraphicsContext gc, GameState state) {
        BoardDrawHelper.drawMarketHud(gc, state, canvas.getHeight());
    }

    private PlacedLink findLink(GameState state, BoardEdge edge) {
        return state.getBoard().getPlacedLinks().stream()
                .filter(l -> l.getEdge().equals(edge))
                .findFirst().orElse(null);
    }

    private PlacedIndustry findPlacedAt(GameState state, CityId city, IndustryType type) {
        return state.getBoard().getPlacedIndustries().stream()
                .filter(p -> p.getCity() == city && p.getType() == type)
                .findFirst().orElse(null);
    }

}
