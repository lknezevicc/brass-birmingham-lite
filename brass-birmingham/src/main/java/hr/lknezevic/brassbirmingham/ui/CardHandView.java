package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.card.Card;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public final class CardHandView extends HBox {

    private final boolean readOnly;
    private final List<CardTile> tiles = new ArrayList<>();
    private CardTile selectedTile;

    public CardHandView() {
        this(false);
    }

    public CardHandView(boolean readOnly) {
        this.readOnly = readOnly;
        setSpacing(8);
        getStyleClass().add("card-hand-view");
    }

    public void update(List<CardDisplayItem> items) {
        getChildren().clear();
        tiles.clear();
        selectedTile = null;

        for (CardDisplayItem item : items) {
            CardTile tile = new CardTile(item);
            if (!readOnly) {
                tile.setOnMouseClicked(e -> selectTile(tile));
            }
            tiles.add(tile);
            getChildren().add(tile);
        }

        if (!readOnly && !tiles.isEmpty()) {
            selectTile(tiles.getFirst());
        }
    }

    public Card getSelectedCard() {
        if (selectedTile == null) return null;
        return selectedTile.getItem().card();
    }

    public int getSelectedIndex() {
        if (selectedTile == null) return -1;
        return tiles.indexOf(selectedTile);
    }

    private void selectTile(CardTile tile) {
        if (selectedTile != null) selectedTile.setSelected(false);
        selectedTile = tile;
        tile.setSelected(true);
    }
}
