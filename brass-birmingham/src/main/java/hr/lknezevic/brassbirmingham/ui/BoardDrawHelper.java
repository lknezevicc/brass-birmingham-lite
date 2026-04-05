package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.game.PlacedIndustry;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public final class BoardDrawHelper {

    private BoardDrawHelper() {}

    public static void drawPlacedTile(GraphicsContext gc, Rectangle2D rect, PlacedIndustry placed) {
        Color typeColor = getIndustryColor(placed.getType());
        Color ownerColor = placed.getOwnerId() == 0 ? BoardColors.PLAYER_1 : BoardColors.PLAYER_2;

        gc.setFill(placed.isFlipped() ? BoardColors.SUCCESS : typeColor);
        gc.fillRoundRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), 6, 6);

        gc.setStroke(ownerColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), 6, 6);

        gc.setFill(BoardColors.TEXT_PRIMARY);
        String label = getTypeAbbr(placed.getType()) + placed.getLevel().name();
        gc.fillText(label, rect.getMinX() + rect.getWidth() / 2, rect.getMinY() + rect.getHeight() / 2 + 3);

        if (!placed.isFlipped() && placed.getRemainingResources() > 0) {
            gc.setFill(BoardColors.ACCENT_BRASS_BRIGHT);
            gc.setFont(Font.font("System", 7));
            gc.fillText("r:" + placed.getRemainingResources(),
                    rect.getMinX() + rect.getWidth() / 2, rect.getMinY() + rect.getHeight() - 4);
            gc.setFont(Font.font("System", 9));
        }
    }

    public static void drawEmptySlot(GraphicsContext gc, Rectangle2D rect, IndustryType type, boolean highlighted) {
        gc.setFill(highlighted ? BoardColors.HIGHLIGHT_VALID : BoardColors.SURFACE_1);
        gc.fillRoundRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), 6, 6);

        gc.setStroke(highlighted ? BoardColors.HIGHLIGHT_STROKE : BoardColors.BORDER_SOFT);
        gc.setLineWidth(highlighted ? 2.5 : 1);
        gc.strokeRoundRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight(), 6, 6);

        gc.setFill(BoardColors.TEXT_MUTED);
        gc.fillText(getTypeAbbr(type), rect.getMinX() + rect.getWidth() / 2, rect.getMinY() + rect.getHeight() / 2 + 3);
    }

    public static void drawMarketHud(GraphicsContext gc, GameState state, double canvasHeight) {
        double x = 10, y = canvasHeight - 70;
        gc.setFont(Font.font("System", 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(BoardColors.SURFACE_2);
        gc.fillRoundRect(x, y, 140, 60, 8, 8);

        gc.setStroke(BoardColors.BORDER_SOFT);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, 140, 60, 8, 8);

        gc.setFill(BoardColors.TEXT_SECONDARY);
        gc.fillText("Market", x + 8, y + 16);
        gc.setFill(BoardColors.ACCENT_COAL);
        gc.fillText("Coal: " + state.getBoard().getMarket().getCoalSupply(), x + 8, y + 32);
        gc.setFill(BoardColors.IRON_WORKS);
        gc.fillText("Iron: " + state.getBoard().getMarket().getIronSupply(), x + 8, y + 48);
        gc.setFill(BoardColors.BREWERY);
        gc.fillText("Beer: " + state.getBoard().getMerchantBeer(), x + 80, y + 32);
    }

    public static Color getIndustryColor(IndustryType type) {
        return switch (type) {
            case COAL_MINE -> BoardColors.COAL_MINE;
            case IRON_WORKS -> BoardColors.IRON_WORKS;
            case BREWERY -> BoardColors.BREWERY;
            case COTTON_MILL -> BoardColors.COTTON_MILL;
        };
    }

    public static String getTypeAbbr(IndustryType type) {
        return switch (type) {
            case COAL_MINE -> "Coal";
            case IRON_WORKS -> "Iron";
            case BREWERY -> "Brew";
            case COTTON_MILL -> "Cot";
        };
    }

    public static String formatCityName(CityId city) {
        String raw = city.name();
        return raw.charAt(0) + raw.substring(1, Math.min(5, raw.length())).toLowerCase();
    }
}
