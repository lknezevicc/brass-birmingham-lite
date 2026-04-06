package hr.lknezevic.brassbirmingham.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public final class CardTile extends VBox {

    private static final String STYLE_CLASS = "card-tile";
    private static final String SELECTED_CLASS = "card-tile-selected";

    private final CardDisplayItem item;
    private boolean selected;

    public CardTile(CardDisplayItem item) {
        this.item = item;
        this.selected = false;

        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER);
        setSpacing(4);

        FontIcon icon = new FontIcon(item.icon());
        icon.setIconSize(18);
        icon.getStyleClass().add("card-icon");

        Label label = new Label(formatLabel(item.label()));
        label.getStyleClass().add("card-label");
        label.setWrapText(true);
        label.setMaxWidth(70);

        getChildren().addAll(icon, label);
    }

    public CardDisplayItem getItem() {
        return item;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            if (!getStyleClass().contains(SELECTED_CLASS)) getStyleClass().add(SELECTED_CLASS);
        } else {
            getStyleClass().remove(SELECTED_CLASS);
        }
    }

    private String formatLabel(String raw) {
        if (raw == null) return "?";
        String lower = raw.replace("_", " ").toLowerCase();
        if (lower.isEmpty()) return "?";
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

}
